package it.uniroma3.cashlytics.Controller;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.cashlytics.DTO.BudgetDTO;
import it.uniroma3.cashlytics.DTO.TransactionDTO;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Service.FinancialAccountService;
import it.uniroma3.cashlytics.Service.MerchantService;

@Controller
public class FinancialAccountController {

    @Autowired
    private FinancialAccountService financialAccountService;
    @Autowired
    private MerchantService merchantService;

    /**
     * GET: Account Details
     */
    @PreAuthorize("@financialAccountService.isAccountOwnedByUser(#accountId, authentication.name)")
    @GetMapping("/{username}/account/{accountId}")
    @Transactional(readOnly = true)
    public String getAccountDetails(@PathVariable String username,
            @PathVariable Long accountId,
            Model model) {
        FinancialAccount account = financialAccountService.getFinancialAccountById(accountId);

        model.addAttribute("account", account);
        model.addAttribute("username", username);
        model.addAttribute("transactions", account.getTransactions());
        model.addAttribute("budgets", account.getBudgets());
        model.addAttribute("merchants", merchantService.findAllByUser(account.getUser()));

        if (!model.containsAttribute("transactionDTO")) {
            TransactionDTO transactionDTO = new TransactionDTO();
            transactionDTO.setDate(LocalDate.now()); // Imposta la data di oggi
            model.addAttribute("transactionDTO", transactionDTO);
        }
        if (!model.containsAttribute("budgetDTO")) {
            BudgetDTO budgetDTO = new BudgetDTO();
            budgetDTO.setDate(LocalDate.now());
            model.addAttribute("budgetDTO", budgetDTO);
        }
        return "accountDetails";
    }

    /**
     * POST: Delete Account
     */
    @PreAuthorize("@financialAccountService.isAccountOwnedByUser(#accountId, authentication.name)")
    @PostMapping("/{username}/dashboard/delete-account/{accountId}")
    public String deleteAccount(@PathVariable String username,
            @PathVariable Long accountId,
            RedirectAttributes redirectAttributes) {
        try {
            financialAccountService.deleteAccount(accountId);
            redirectAttributes.addFlashAttribute("successMessage", "Account eliminato con successo.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore: " + e.getMessage());
        }
        return "redirect:/" + username + "/dashboard";
    }

}
