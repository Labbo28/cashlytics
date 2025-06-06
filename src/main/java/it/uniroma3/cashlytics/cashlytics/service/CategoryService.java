package it.uniroma3.cashlytics.cashlytics.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.cashlytics.cashlytics.Model.Category;
import it.uniroma3.cashlytics.cashlytics.Model.User;
import it.uniroma3.cashlytics.cashlytics.Repository.CategoryRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Restituisce tutte le categorie associate all’utente corrente.
     */
    public Set<Category> findAllByUser(User currentUser) {
        return categoryRepository.findAllByUser(currentUser);
    }

    /**
     * Cerca una Category per ID verificando che appartenga all’utente passato.
     */
    public Optional<Category> findByIdAndUser(Long catId, User user) {
        return categoryRepository.findByIdAndUser(catId, user);
    }

    /**
     * Cerca una Category per nome (case-insensitive) verificando che appartenga
     * all’utente passato.
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
}
