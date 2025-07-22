package it.uniroma3.cashlytics.Controller;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.cashlytics.DTO.CategoryDTO;
import it.uniroma3.cashlytics.Model.Category;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Service.CategoryService;
import it.uniroma3.cashlytics.Service.UserService;
import jakarta.validation.Valid;

@Controller
public class CategoryController {

	
	@Autowired
	private UserService userService;
	@Autowired
	private CategoryService categoryService;

	/*
	 * GET: Lista categorie utente
	 */
	@GetMapping("/{username}/categories")
	@Transactional(readOnly = true)
	public String listCategories(@PathVariable String username, Model model, RedirectAttributes redirectAttributes) {
		User currentUser = userService.getUserByUsername(username);
		model.addAttribute("categories", categoryService.findMainCategoriesByUser(currentUser));
		model.addAttribute("allCategories", categoryService.findAllByUser(currentUser));

		if (!model.containsAttribute("categoryDTO")) {
			model.addAttribute("categoryDTO", new CategoryDTO());
		}
		return "categories.html";
	}


	 
	  @PostMapping("/{username}/categories/add")
	  
	  @Transactional
	  public String addCategory(@PathVariable String username,
	  
	  @Valid CategoryDTO categoryDTO,
	  BindingResult bindingResult,
	  RedirectAttributes redirectAttributes) {
	  if (bindingResult.hasErrors()) {
	  redirectAttributes.addFlashAttribute(
	  "org.springframework.validation.BindingResult.categoryDTO",
	  bindingResult);
	  redirectAttributes.addFlashAttribute("categoryDTO", categoryDTO);
	  return "redirect:/" + username + "/categories";
	  }
	  User currentUser = userService.getUserByUsername(username);
	  Category newCategory = categoryService.createCategory(categoryDTO,
	  currentUser, bindingResult);
	  
	  if (newCategory == null) {
	  redirectAttributes.addFlashAttribute("errorMessage",
	  "Non è stato possibile creare la categoria.");
	  return "redirect:/" + username + "/categories";
	  }
	  redirectAttributes.addFlashAttribute("successMessage",
	  "Categoria creata con successo!");
	  
	  return "redirect:/" + username + "/categories";
	  }
	 

	/*
	 * POST: Cancella categoria esistente
	 */
	@PostMapping("/{username}/categories/{categoryId}/delete")
	@Transactional
	public String deleteCategory(@PathVariable String username,
			@PathVariable Long categoryId,
			RedirectAttributes redirectAttributes) {
		try {
			categoryService.deleteCategory(categoryId);
			redirectAttributes.addFlashAttribute("successMessage", "Categoria cancellata con successo.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("successMessage", "Non è stato possibile cancellare la categoria.");
			System.out.println("Errore: " + e.getMessage());
		}
		return "redirect:/" + username + "/categories";
	}

	/*
	 * GET: Form modifica categoria
	 */
	@GetMapping("/{username}/categories/{categoryId}/edit")
	public String editCategoryForm(@PathVariable String username,
			@PathVariable Long categoryId,
			Model model,
			RedirectAttributes redirectAttributes) {
		Optional<Category> categoryOpt = categoryService.findById(categoryId);
		if (categoryOpt.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Categoria non trovata.");
			return "redirect:/" + username + "/categories";
		}

		Category category = categoryOpt.get();
		CategoryDTO categoryDTO = new CategoryDTO();
		User currentUser = userService.getUserByUsername(username);

		categoryDTO.setId(category.getId());
		categoryDTO.setName(category.getName());
		categoryDTO.setIcon(category.getIcon());
		categoryDTO.setColor(category.getColor());
		if (category.getParentCategory() != null) {
			categoryDTO.setParentCategoryId(category.getParentCategory().getId());
		}
		model.addAttribute("username", username);
		model.addAttribute("category", category);
		model.addAttribute("categoryDTO", categoryDTO);
		model.addAttribute("categories", categoryService.findMainCategoriesByUser(currentUser));

		return "edit-category";
	}

	/*
	 * POST: Modifica categoria esistente
	 */
	@PostMapping("/{username}/categories/{categoryId}/edit")
	@Transactional
	public String editCategory(@PathVariable String username,
			@PathVariable Long categoryId,
			@Valid CategoryDTO categoryDTO,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes redirectAttributes) {
		Optional<Category> categoryOpt = categoryService.findById(categoryId);
		if (bindingResult.hasErrors()) {
			if (categoryOpt.isPresent()) {
				Category category = categoryOpt.get();
				User currentUser = userService.getUserByUsername(username);

				model.addAttribute("username", username);
				model.addAttribute("category", category);
				model.addAttribute("categoryDTO", categoryDTO);
				model.addAttribute("categories", categoryService.findMainCategoriesByUser(currentUser));
			}
			return "edit-category";
		}

		if (categoryOpt.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Categoria non trovata.");
			return "redirect:/" + username + "/categories";
		}
		Category category = categoryOpt.get();
		User currentUser = userService.getUserByUsername(username);

		categoryService.updateCategory(category, categoryDTO, currentUser, bindingResult);
		redirectAttributes.addFlashAttribute("successMessage", "Categoria aggiornata con successo!");
		return "redirect:/" + username + "/categories";
	}

}
