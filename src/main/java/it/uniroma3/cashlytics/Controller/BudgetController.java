package it.uniroma3.cashlytics.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.cashlytics.DTO.BudgetDTO;
import it.uniroma3.cashlytics.Model.Budget;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Service.BudgetService;
import it.uniroma3.cashlytics.Service.CategoryService;
import it.uniroma3.cashlytics.Service.FinancialAccountService;
import it.uniroma3.cashlytics.Service.MerchantService;
import it.uniroma3.cashlytics.Service.UserService;
import jakarta.validation.Valid;

@Controller
public class BudgetController {

	@Autowired
	private BudgetService budgetService;
	@Autowired
	private FinancialAccountService financialAccountService;
	@Autowired
	private UserService userService;
	@Autowired
	private MerchantService merchantService;
	@Autowired
	private CategoryService categoryService;

	/*
	 * POST: Crea nuovo budget
	 */
	@PostMapping("/{username}/account/{accountId}/add-budget")
	@Transactional
	public String addBudget(@PathVariable String username,
			@PathVariable Long accountId,
			@Valid BudgetDTO budgetDTO,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			redirectAttributes.addFlashAttribute(
					"org.springframework.validation.BindingResult.budgetDTO", bindingResult);
			redirectAttributes.addFlashAttribute("budgetDTO", budgetDTO);
			return "redirect:/" + username + "/account/" + accountId;
		}

		try {
			FinancialAccount account = financialAccountService.getFinancialAccountById(accountId);
			User user = userService.getUserByUsername(username);
			Budget newBudget = budgetService.createBudget(budgetDTO, account, user);

			if (newBudget == null) {
				redirectAttributes.addFlashAttribute("errorMessage", "Non è stato possibile creare il budget.");
				return "redirect:/" + username + "/account/" + accountId;
			}

			redirectAttributes.addFlashAttribute("successMessage", "Budget creato con successo!");
			return "redirect:/" + username + "/account/" + accountId;
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			redirectAttributes.addFlashAttribute("budgetDTO", budgetDTO);
			return "redirect:/" + username + "/account/" + accountId;
		}
	}

	/*
	 * POST: Cancella budget esistente
	 */
	@PostMapping("/{username}/account/{accountId}/delete-budget/{budgetId}")
	public String deleteBudget(@PathVariable String username,
			@PathVariable Long accountId,
			@PathVariable Long budgetId,
			RedirectAttributes redirectAttributes) {
		try {
			FinancialAccount account = financialAccountService.getFinancialAccountById(accountId);
			budgetService.deleteBudget(budgetId, account);
			redirectAttributes.addFlashAttribute("successMessage", "Budget cancellato con successo.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Non è stato possibile cancellare il budget.");
			System.out.println("Errore: " + e.getMessage());
		}
		return "redirect:/" + username + "/account/" + accountId;
	}

	/**
	 * GET: Modifica budget esistente
	 */
	@org.springframework.web.bind.annotation.GetMapping("/{username}/account/{accountId}/edit-budget/{budgetId}")
	public String editBudgetForm(@PathVariable String username,
								 @PathVariable Long accountId,
								 @PathVariable Long budgetId,
								 org.springframework.ui.Model model,
								 RedirectAttributes redirectAttributes) {
		java.util.Optional<Budget> budgetOpt = budgetService.findById(budgetId);
		if (budgetOpt.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Budget non trovato.");
			return "redirect:/" + username + "/account/" + accountId;
		}
		Budget budget = budgetOpt.get();
		FinancialAccount account = financialAccountService.getFinancialAccountById(accountId);
		User user = userService.getUserByUsername(username);

		BudgetDTO budgetDTO = new BudgetDTO();
		budgetDTO.setAmount(budget.getAmount());
		budgetDTO.setDescription(budget.getDescription());
		if (budget.getDate() != null) {
			budgetDTO.setDate(budget.getDate().toLocalDate());
		}
		budgetDTO.setRecurrencePattern(budget.getRecurrence());
		if (budget.getCategory() != null) {
			budgetDTO.setCategoryId(budget.getCategory().getId());
		}
		model.addAttribute("budget", budget);
		model.addAttribute("budgetDTO", budgetDTO);
		model.addAttribute("account", account);
		model.addAttribute("username", username);
		model.addAttribute("categories", categoryService.findMainCategoriesByUser(user));
		return "edit-budget";
	}

	/**
	 * POST: Modifica budget esistente
	 */
	@PostMapping("/{username}/account/{accountId}/edit-budget/{budgetId}")
	@Transactional
	public String editBudget(@PathVariable String username,
							@PathVariable Long accountId,
							@PathVariable Long budgetId,
							@Valid BudgetDTO budgetDTO,
							BindingResult bindingResult,
							org.springframework.ui.Model model,
							RedirectAttributes redirectAttributes) {
		java.util.Optional<Budget> budgetOpt = budgetService.findById(budgetId);
		if (bindingResult.hasErrors()) {
			if (budgetOpt.isPresent()) {
				Budget budget = budgetOpt.get();
				FinancialAccount account = financialAccountService.getFinancialAccountById(accountId);
				User user = userService.getUserByUsername(username);
				model.addAttribute("budget", budget);
				model.addAttribute("account", account);
				model.addAttribute("username", username);
				model.addAttribute("categories", categoryService.findMainCategoriesByUser(user));
			}
			return "edit-budget";
		}
		if (budgetOpt.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage", "Budget non trovato.");
			return "redirect:/" + username + "/account/" + accountId;
		}
		Budget budget = budgetOpt.get();
		// Update fields
		budget.setAmount(budgetDTO.getAmount());
		budget.setDescription(budgetDTO.getDescription());
		if (budgetDTO.getDate() != null) {
			budget.setDate(budgetDTO.getDate().atStartOfDay());
		}
		budget.setRecurrence(budgetDTO.getRecurrencePattern());
		if (budgetDTO.getCategoryId() != null) {
			budget.setCategory(categoryService.findById(budgetDTO.getCategoryId()).orElse(null));
		}
		budget.setFinancialAccount(financialAccountService.getFinancialAccountById(accountId)); // Ensure association is maintained
		budgetService.save(budget);
		redirectAttributes.addFlashAttribute("successMessage", "Budget aggiornato con successo!");
		return "redirect:/" + username + "/account/" + accountId;
	}

}
