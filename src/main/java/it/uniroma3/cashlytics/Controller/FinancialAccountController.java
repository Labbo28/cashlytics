package it.uniroma3.cashlytics.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.cashlytics.DTO.BudgetDTO;
import it.uniroma3.cashlytics.DTO.TransactionDTO;
import it.uniroma3.cashlytics.Model.Budget;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.Transaction;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import it.uniroma3.cashlytics.Service.BudgetService;
import it.uniroma3.cashlytics.Service.FinancialAccountService;
import it.uniroma3.cashlytics.Service.MerchantService;
import it.uniroma3.cashlytics.Service.TransactionService;
import it.uniroma3.cashlytics.Service.UserService;
import jakarta.validation.Valid;

@Controller
public class FinancialAccountController {

    @Autowired
    private FinancialAccountService financialAccountService;
    @Autowired
    private UserService userService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private BudgetService budgetService;
    @Autowired
    private MerchantService merchantService;

    /**
     * GET: Account Details
     */
    @PreAuthorize("@financialAccountService.isAccountOwnedByUser(#accountId, authentication.name)")
    @GetMapping("/{username}/account/{accountId}")
    @Transactional(readOnly = true)
    public String getAccountDetails(@PathVariable String username, @PathVariable Long accountId, Model model) {
        User currentUser = userService.getUserByUsername(username);
        FinancialAccount account = financialAccountService.getFinancialAccountByUsername(accountId, username);

        model.addAttribute("account", account);
        model.addAttribute("username", username);
        model.addAttribute("transactions", account.getTransactions());
        model.addAttribute("budgets", account.getBudgets());
        model.addAttribute("merchants", merchantService.findAllByUser(currentUser));

        // Add DTOs for forms if not already present
        if (!model.containsAttribute("transactionDTO")) {
            TransactionDTO transactionDTO = new TransactionDTO();
            transactionDTO.setDate(LocalDate.now());
            model.addAttribute("transactionDTO", transactionDTO);
        }
        if (!model.containsAttribute("budgetDTO")) {
            model.addAttribute("budgetDTO", new BudgetDTO());
        }

        return "accountDetails";
    }

    /**
     * POST: Add new transaction
     */
    @PreAuthorize("@financialAccountService.isAccountOwnedByUser(#accountId, authentication.name)")
    @PostMapping("/{username}/account/{accountId}/add-transaction")
    @Transactional
    public String addTransaction(@PathVariable String username,
            @PathVariable Long accountId,
            @Valid TransactionDTO transactionDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.transactionDTO", bindingResult);
            redirectAttributes.addFlashAttribute("transactionDTO", transactionDTO);
            return "redirect:/" + username + "/account/" + accountId;
        }

        User currentUser = userService.getUserByUsername(username);
        FinancialAccount account = financialAccountService.getFinancialAccountByUsername(accountId, username);

        Transaction newTransaction = transactionService.createTransaction(transactionDTO, account, currentUser);

        if (newTransaction == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Non è stato possibile aggiungere la transazione.");
            redirectAttributes.addFlashAttribute("transactionDTO", transactionDTO);
            return "redirect:/" + username + "/account/" + accountId;
        }

        redirectAttributes.addFlashAttribute("successMessage", "Transazione aggiunta con successo!");
        return "redirect:/" + username + "/account/" + accountId;
    }

    /**
     * GET: Show recurring transactions
     */
    @PreAuthorize("@financialAccountService.isAccountOwnedByUser(#accountId, authentication.name)")
    @GetMapping("/{username}/account/{accountId}/recurring")
    @Transactional(readOnly = true)
    public String showRecurringTransactions(@PathVariable String username,
            @PathVariable Long accountId,
            Model model) {

        FinancialAccount account = financialAccountService.getFinancialAccountByUsername(accountId, username);

        Set<Transaction> allTransactions = account.getTransactions();
        List<Transaction> recurringTransactions = allTransactions.stream()
                .filter(tx -> tx.getRecurrence() != RecurrencePattern.UNA_TANTUM)
                .toList();

        model.addAttribute("account", account);
        model.addAttribute("transactions", recurringTransactions);
        model.addAttribute("username", username);

        return "recurring-transactions";
    }

    /**
     * POST: Delete transaction
     */
    @PreAuthorize("@transactionService.isTransactionOwnedByUser(#transactionId, authentication.name)")
    @PostMapping("/{username}/account/{accountId}/delete-transaction/{transactionId}")
    @Transactional
    public String deleteTransaction(@PathVariable String username,
            @PathVariable Long accountId,
            @PathVariable Long transactionId,
            RedirectAttributes redirectAttributes) {

        transactionService.deleteTransaction(transactionId);
        redirectAttributes.addFlashAttribute("successMessage", "Transazione rimossa con successo.");
        return "redirect:/" + username + "/account/" + accountId;
    }

    /**
     * GET: Edit transaction form
     */
    @PreAuthorize("@transactionService.isTransactionOwnedByUser(#transactionId, authentication.name)")
    @GetMapping("/{username}/account/{accountId}/edit-transaction/{transactionId}")
    @Transactional(readOnly = true)
    public String editTransactionForm(@PathVariable String username,
            @PathVariable Long accountId,
            @PathVariable Long transactionId,
            Model model) {

        User currentUser = userService.getUserByUsername(username);
        FinancialAccount account = financialAccountService.getFinancialAccountByUsername(accountId, username);
        Transaction transaction = transactionService.getTransactionByUsername(transactionId, username);

        TransactionDTO transactionDTO = transactionService.convertToDTO(transaction);

        model.addAttribute("transaction", transaction);
        model.addAttribute("transactionDTO", transactionDTO);
        model.addAttribute("account", account);
        model.addAttribute("username", username);
        model.addAttribute("merchants", merchantService.findAllByUser(currentUser));

        return "edit-transaction";
    }

    /**
     * POST: Update transaction
     */
    @PreAuthorize("@transactionService.isTransactionOwnedByUser(#transactionId, authentication.name)")
    @PostMapping("/{username}/account/{accountId}/edit-transaction/{transactionId}")
    @Transactional
    public String editTransaction(@PathVariable String username,
            @PathVariable Long accountId,
            @PathVariable Long transactionId,
            @ModelAttribute @Valid TransactionDTO transactionDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            User currentUser = userService.getUserByUsername(username);
            FinancialAccount account = financialAccountService.getFinancialAccountByUsername(accountId, username);
            Transaction transaction = transactionService.getTransactionByUsername(transactionId, username);

            model.addAttribute("transaction", transaction);
            model.addAttribute("account", account);
            model.addAttribute("username", username);
            model.addAttribute("merchants", merchantService.findAllByUser(currentUser));
            return "edit-transaction";
        }

        User currentUser = userService.getUserByUsername(username);
        Transaction transaction = transactionService.getTransactionByUsername(transactionId, username);

        transactionService.updateTransaction(transaction, transactionDTO, transaction.getAmount(), currentUser);
        redirectAttributes.addFlashAttribute("successMessage", "Transazione aggiornata con successo.");
        return "redirect:/" + username + "/account/" + accountId;
    }

    /**
     * POST: Add new budget
     */
    @PreAuthorize("@financialAccountService.isAccountOwnedByUser(#accountId, authentication.name)")
    @PostMapping("/{username}/account/{accountId}/add-budget")
    @Transactional
    public String addBudget(@PathVariable String username,
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
            return "redirect:/" + username + "/account/" + accountId;
        }

        User currentUser = userService.getUserByUsername(username);
        FinancialAccount account = financialAccountService.getFinancialAccountByUsername(accountId, username);

        Budget newBudget = budgetService.createBudget(budgetDTO, account, currentUser, bindingResult);

        if (bindingResult.hasErrors() || newBudget == null) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.budgetDTO", bindingResult);
            redirectAttributes.addFlashAttribute("budgetDTO", budgetDTO);
            if (newBudget == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Non è stato possibile creare il budget.");
            }
            return "redirect:/" + username + "/account/" + accountId;
        }

        redirectAttributes.addFlashAttribute("successMessage", "Budget aggiunto con successo!");
        return "redirect:/" + username + "/account/" + accountId;
    }

    /**
     * POST: Delete account
     */
    @PreAuthorize("@financialAccountService.isAccountOwnedByUser(#accountId, authentication.name)")
    @PostMapping("/{username}/dashboard/delete-account/{accountId}")
    @Transactional
    public String deleteAccount(@PathVariable String username,
            @PathVariable Long accountId,
            RedirectAttributes redirectAttributes) {

        financialAccountService.deleteAccountForUser(accountId, username);
        redirectAttributes.addFlashAttribute("successMessage", "Account eliminato con successo.");
        return "redirect:/" + username + "/dashboard";
    }

    /**
     * POST: Delete budget
     */
    @PreAuthorize("@budgetService.isBudgetOwnedByUser(#budgetId, authentication.name)")
    @PostMapping("/{username}/account/{accountId}/delete-budget/{budgetId}")
    @Transactional
    public String deleteBudget(@PathVariable String username,
            @PathVariable Long accountId,
            @PathVariable Long budgetId,
            RedirectAttributes redirectAttributes) {

        budgetService.deleteBudgetForUser(budgetId, username);
        redirectAttributes.addFlashAttribute("successMessage", "Budget eliminato con successo.");
        return "redirect:/" + username + "/account/" + accountId;
    }

}
