package it.uniroma3.cashlytics.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import it.uniroma3.cashlytics.DTO.CategorizableDTO;
import it.uniroma3.cashlytics.DTO.CategoryDTO;
import it.uniroma3.cashlytics.DTO.TransactionDTO;
import it.uniroma3.cashlytics.Model.Category;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Repository.CategoryRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Restituisce tutte le categorie associate all'utente corrente.
     */
    public Set<Category> findAllByUser(User currentUser) {
        return categoryRepository.findAllByUser(currentUser);
    }

    /**
     * Restituisce solo le categorie principali (senza parent) dell'utente.
     */
    public List<Category> findMainCategoriesByUser(User currentUser) {
        return categoryRepository.findAllByUser(currentUser).stream()
                .filter(cat -> cat.getParentCategory() == null)
                .collect(Collectors.toList());
    }

    /**
     * Cerca una Category per ID verificando che appartenga all'utente passato.
     */
    public Optional<Category> findByIdAndUser(Long catId, User user) {
        return categoryRepository.findByIdAndUser(catId, user);
    }

    /**
     * Cerca una Category per nome (case-insensitive) verificando che appartenga
     * all'utente passato.
     */
    public Optional<Category> findByNameAndUser(String catName, User user) {
        return categoryRepository.findByNameIgnoreCaseAndUser(catName, user);
    }

    /**
     * Salva (inserisce o aggiorna) una Category nel repository.
     */
    public Category save(Category newCat) {
        return categoryRepository.save(newCat);
    }

    /**
     * Crea una nuova categoria da DTO.
     * public Category createCategory(CategoryDTO categoryDTO, User user,
     * BindingResult bindingResult) {
     * // Verifica se esiste già una categoria con lo stesso nome
     * Optional<Category> existingCategory =
     * findByNameAndUser(categoryDTO.getName(), user);
     * if (existingCategory.isPresent()) {
     * bindingResult.rejectValue("name", "error.categoryDTO", "Una categoria con
     * questo nome esiste già.");
     * return null;
     * }
     * 
     * Category category = new Category();
     * category.setName(categoryDTO.getName());
     * category.setIcon(categoryDTO.getIcon());
     * category.setColor(categoryDTO.getColor());
     * category.setUser(user);
     * 
     * // Gestione categoria padre se specificata
     * if (categoryDTO.getParentCategoryId() != null) {
     * Optional<Category> parentOpt =
     * findByIdAndUser(categoryDTO.getParentCategoryId(), user);
     * if (parentOpt.isPresent()) {
     * category.setParentCategory(parentOpt.get());
     * } else {
     * bindingResult.rejectValue("parentCategoryId", "error.categoryDTO", "Categoria
     * padre non valida.");
     * return null;
     * }
     * }
     * return save(category);
     * }
     */

    /**
     * Aggiorna una categoria esistente.
     */
    public void updateCategory(Category category, CategoryDTO categoryDTO, User user, BindingResult bindingResult) {
        // Verifica se esiste già una categoria con lo stesso nome (escludendo quella
        // corrente)
        Optional<Category> existingCategory = findByNameAndUser(categoryDTO.getName(), user);
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(category.getId())) {
            bindingResult.rejectValue("name", "error.categoryDTO", "Una categoria con questo nome esiste già.");
            return;
        }

        category.setName(categoryDTO.getName());
        category.setIcon(categoryDTO.getIcon());
        category.setColor(categoryDTO.getColor());

        // Gestione categoria padre
        if (categoryDTO.getParentCategoryId() != null) {
            Optional<Category> parentOpt = findByIdAndUser(categoryDTO.getParentCategoryId(), user);
            if (parentOpt.isPresent()) {
                // Verifica che non si stia creando un ciclo
                if (!isCircularReference(category, parentOpt.get())) {
                    category.setParentCategory(parentOpt.get());
                } else {
                    bindingResult.rejectValue("parentCategoryId", "error.categoryDTO",
                            "Non è possibile creare riferimenti circolari.");
                    return;
                }
            } else {
                bindingResult.rejectValue("parentCategoryId", "error.categoryDTO", "Categoria padre non valida.");
                return;
            }
        } else {
            category.setParentCategory(null);
        }
        save(category);
    }

    /**
     * Elimina una categoria e tutte le sue sottocategorie.
     */
    public void deleteCategory(Long categoryId) {
        Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
        if (categoryOpt.isPresent()) {
            Category category = categoryOpt.get();

            // Rimuovi i riferimenti dalle transazioni
            if (category.getTransactions() != null) {
                category.getTransactions().forEach(transaction -> transaction.setCategory(null));
            }

            categoryRepository.delete(category);
        }
    }

    /**
     * Risolve o crea una categoria basata sui dati del DTO per le transazioni.
     */
    public Category resolveOrCreateCategory(CategorizableDTO dto, User user, BindingResult bindingResult) {
        // Caso 1: ID categoria fornito → cerco quella categoria
        Long categoryId = dto.getCategoryId();
        if (categoryId != null) {
            Optional<Category> opt = findByIdAndUser(categoryId, user);
            if (opt.isPresent()) {
                return opt.get();
            } else {
                bindingResult.rejectValue("categoryId", "error.transactionDTO", "Categoria non valida.");
                return null;
            }
        }

        // Estrai i campi (name, icon, color)
        String name = dto.getCategoryName() != null ? dto.getCategoryName().trim() : "";
        String icon = dto.getCategoryIcon() != null ? dto.getCategoryIcon().trim() : "";
        String color = dto.getCategoryColor() != null ? dto.getCategoryColor().trim() : "";
        // Se nessun campo è compilato, ignora la categoria
        boolean allEmpty = name.isBlank() && icon.isBlank() && color.isBlank();
        if (allEmpty) {
            return null;
        }
        // Se è stata richiesta una categoria ma manca il nome → ERRORE
        if (name.isBlank()) {
            bindingResult.rejectValue("categoryName", "error.transactionDTO",
                    "Inserisci il nome della categoria.");
            return null;
        }

        // Categoria con nome esistente → restituiscila
        Optional<Category> optByName = findByNameAndUser(name, user);
        if (optByName.isPresent()) {
            return optByName.get();
        }

        // Crea nuova categoria (nome valido, icona/colore opzionali)
        Category newCat = new Category();
        newCat.setName(name);
        newCat.setIcon(!icon.isBlank() ? icon : "fas fa-euro-sign");
        newCat.setColor(!color.isBlank() ? color : "#718096");
        newCat.setUser(user);
        return save(newCat);
    }

    /**
     * Verifica se impostare parentCategory creerebbe un riferimento circolare.
     */
    private boolean isCircularReference(Category category, Category potentialParent) {
        Category current = potentialParent;
        while (current != null) {
            if (current.getId().equals(category.getId())) {
                return true;
            }
            current = current.getParentCategory();
        }
        return false;
    }

    /**
     * Trova una categoria per ID.
     */
    public Optional<Category> findById(Long categoryId) {
        return categoryRepository.findById(categoryId);
    }

}
