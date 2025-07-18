package it.uniroma3.cashlytics.Controller;


import java.time.LocalDate;

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
import it.uniroma3.cashlytics.Service.FinancialAccountService;
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

	@PostMapping("/{username}/account/{accountId}/add-budget")
	@Transactional
	public String addBudget(
			@PathVariable String username,
			@PathVariable Long accountId,
			@Valid BudgetDTO budgetDTO,
			BindingResult bindingResult,
			RedirectAttributes redirectAttributes) {

		if (budgetDTO.getDate() == null) {
			budgetDTO.setDate(LocalDate.now());
		}

		if (bindingResult.hasErrors()) {
			redirectAttributes.addFlashAttribute(
					"org.springframework.validation.BindingResult.budgetDTO", bindingResult);
			redirectAttributes.addFlashAttribute("budgetDTO", budgetDTO);
			redirectAttributes.addFlashAttribute("errorMessage", "Please correct the errors in the form.");
			return "redirect:/" + username + "/account/" + accountId;
		}

		FinancialAccount account = financialAccountService.getFinancialAccountById(accountId);
		User user = userService.getUserByUsername(username);

		Budget newBudget = budgetService.createBudget(budgetDTO, account, user);

		if (newBudget == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Failed to create budget.");
			return "redirect:/" + username + "/account/" + accountId;
		}

		redirectAttributes.addFlashAttribute("successMessage", "Budget added successfully!");
		return "redirect:/" + username + "/account/" + accountId;
	}

	@PostMapping("/{username}/account/{accountId}/delete-budget/{budgetId}")
	public String deleteBudget(
			@PathVariable String username,
			@PathVariable Long accountId,
			@PathVariable Long budgetId,
			RedirectAttributes redirectAttributes) {

		try {
			FinancialAccount account = financialAccountService.getFinancialAccountById(accountId);
			budgetService.deleteBudget(budgetId, account);
			redirectAttributes.addFlashAttribute("successMessage", "Budget eliminato con successo.");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Errore: " + e.getMessage());
		}

		return "redirect:/" + username + "/account/" + accountId;
	}

}

    @Autowired
    private BudgetService budgetService;
    @Autowired
    private FinancialAccountService financialAccountService;
    @Autowired
    private UserService userService;

    @PostMapping("/{username}/account/{accountId}/add-budget")
    @Transactional
    public String addBudget(
            @PathVariable String username,
            @PathVariable Long accountId,
            @Valid BudgetDTO budgetDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (budgetDTO.getDate() == null) {
            budgetDTO.setDate(LocalDate.now());
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.budgetDTO", bindingResult);
            redirectAttributes.addFlashAttribute("budgetDTO", budgetDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the errors in the form.");
            return "redirect:/" + username + "/account/" + accountId;
        }

        FinancialAccount account = financialAccountService.getFinancialAccountById(accountId);
        User user = userService.getUserByUsername(username);
        
        Budget newBudget = budgetService.createBudget(budgetDTO, account, user);

        if (newBudget == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create budget.");
            return "redirect:/" + username + "/account/" + accountId;
        }

        redirectAttributes.addFlashAttribute("successMessage", "Budget added successfully!");
        return "redirect:/" + username + "/account/" + accountId;
    }

    @PostMapping("/{username}/account/{accountId}/delete-budget/{budgetId}")
    public String deleteBudget(
            @PathVariable String username,
            @PathVariable Long accountId,
            @PathVariable Long budgetId,
            RedirectAttributes redirectAttributes) {

        try {
            budgetService.deleteBudget(budgetId);
            redirectAttributes.addFlashAttribute("successMessage", "Budget eliminato con successo.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore: " + e.getMessage());
        }

        return "redirect:/" + username + "/account/" + accountId;
    }
}

