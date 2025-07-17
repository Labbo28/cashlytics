package it.uniroma3.cashlytics.Controller;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import it.uniroma3.cashlytics.DTO.CategoryDTO;
import it.uniroma3.cashlytics.DTO.TransactionDTO;
import it.uniroma3.cashlytics.Model.Budget;
import it.uniroma3.cashlytics.Model.Category;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.Merchant;
import it.uniroma3.cashlytics.Model.Transaction;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import it.uniroma3.cashlytics.Service.BudgetService;
import it.uniroma3.cashlytics.Service.CategoryService;
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
    @Autowired
    private CategoryService categoryService;

    /*
     * GET: Account Details
     */
    @GetMapping("/{username}/account/{accountId}")
    @Transactional(readOnly = true)
    public String getAccountDetails(
            @PathVariable String username,
            @PathVariable Long accountId,
            Model model,
            RedirectAttributes redirectAttributes) {

        // 1. Autenticazione e autorizzazione
        User currentUser = getAuthenticatedUserOrRedirect(username, redirectAttributes);
        if (currentUser == null) {
            return "redirect:/login";
        }

        // 2. Caricamento account
        FinancialAccount account = financialAccountService.getFinancialAccountById(accountId);
        if (!isAccountOwnedByUser(account, currentUser, redirectAttributes, username)) {
            return "redirect:/" + username + "/dashboard";
        }

        // 3. Transazioni e Dati aggiuntivi
        model.addAttribute("account", account);
        model.addAttribute("username", username);
        model.addAttribute("transactions", account.getTransactions());
        model.addAttribute("budgets", account.getBudgets());

        // 4. Elenchi di categorie e merchant per l'utente
        model.addAttribute("categories", categoryService.findAllByUser(currentUser));
        model.addAttribute("merchants", merchantService.findAllByUser(currentUser));

        // 5. DTO per i form (solo se non già presente)
        if (!model.containsAttribute("transactionDTO")) {
            model.addAttribute("transactionDTO", new TransactionDTO());
        }
        if (!model.containsAttribute("budgetDTO")) {
            model.addAttribute("budgetDTO", new BudgetDTO());
        }
        if (!model.containsAttribute("categoryDTO")) {
            model.addAttribute("categoryDTO", new CategoryDTO());
        }

        return "accountDetails";
    }

// ============================================================================
// === Section: Categories
// ============================================================================

    /*
     * GET: Lista categorie utente
     */
    @GetMapping("/{username}/categories")
    @Transactional(readOnly = true)
    public String listCategories(@PathVariable String username, Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedUserOrRedirect(username, redirectAttributes);
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("username", username);
        model.addAttribute("categories", categoryService.findMainCategoriesByUser(currentUser));
        model.addAttribute("allCategories", categoryService.findAllByUser(currentUser));

        if (!model.containsAttribute("categoryDTO")) {
            model.addAttribute("categoryDTO", new CategoryDTO());
        }

        return "categories";
    }

    /*
     * POST: Aggiungi nuova categoria
     */
    @PostMapping("/{username}/categories/add")
    @Transactional
    public String addCategory(
            @PathVariable String username,
            @Valid CategoryDTO categoryDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        User currentUser = getAuthenticatedUserOrRedirect(username, redirectAttributes);
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.categoryDTO", bindingResult);
            redirectAttributes.addFlashAttribute("categoryDTO", categoryDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Correggi gli errori nel form.");
            return "redirect:/" + username + "/categories";
        }

        Category newCategory = categoryService.createCategory(categoryDTO, currentUser, bindingResult);

        if (bindingResult.hasErrors() || newCategory == null) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.categoryDTO", bindingResult);
            redirectAttributes.addFlashAttribute("categoryDTO", categoryDTO);
            return "redirect:/" + username + "/categories";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Categoria creata con successo!");
        return "redirect:/" + username + "/categories";
    }

    /*
     * GET: Form modifica categoria
     */
    @GetMapping("/{username}/categories/edit/{categoryId}")
    public String editCategoryForm(
            @PathVariable String username,
            @PathVariable Long categoryId,
            Model model,
            RedirectAttributes redirectAttributes) {

        User currentUser = getAuthenticatedUserOrRedirect(username, redirectAttributes);
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Category> categoryOpt = categoryService.findByIdAndUser(categoryId, currentUser);
        if (categoryOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Categoria non trovata!");
            return "redirect:/" + username + "/categories";
        }

        Category category = categoryOpt.get();
        CategoryDTO categoryDTO = new CategoryDTO();
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
     * POST: Aggiorna categoria
     */
    @PostMapping("/{username}/categories/edit/{categoryId}")
    @Transactional
    public String editCategory(
            @PathVariable String username,
            @PathVariable Long categoryId,
            @Valid CategoryDTO categoryDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        User currentUser = getAuthenticatedUserOrRedirect(username, redirectAttributes);
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Category> categoryOpt = categoryService.findByIdAndUser(categoryId, currentUser);
        if (categoryOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Categoria non trovata!");
            return "redirect:/" + username + "/categories";
        }

        Category category = categoryOpt.get();

        if (bindingResult.hasErrors()) {
            model.addAttribute("username", username);
            model.addAttribute("category", category);
            model.addAttribute("categoryDTO", categoryDTO);
            model.addAttribute("categories", categoryService.findMainCategoriesByUser(currentUser));
            return "edit-category";
        }

        categoryService.updateCategory(category, categoryDTO, currentUser, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("username", username);
            model.addAttribute("category", category);
            model.addAttribute("categoryDTO", categoryDTO);
            model.addAttribute("categories", categoryService.findMainCategoriesByUser(currentUser));
            return "edit-category";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Categoria aggiornata con successo!");
        return "redirect:/" + username + "/categories";
    }

    /*
     * POST: Elimina categoria
     */
    @PostMapping("/{username}/categories/delete/{categoryId}")
    @Transactional
    public String deleteCategory(
            @PathVariable String username,
            @PathVariable Long categoryId,
            RedirectAttributes redirectAttributes) {

        User currentUser = getAuthenticatedUserOrRedirect(username, redirectAttributes);
        if (currentUser == null) {
            return "redirect:/login";
        }

        Optional<Category> categoryOpt = categoryService.findByIdAndUser(categoryId, currentUser);
        if (categoryOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Categoria non trovata!");
            return "redirect:/" + username + "/categories";
        }

        categoryService.deleteCategory(categoryId);
        redirectAttributes.addFlashAttribute("successMessage", "Categoria eliminata con successo!");
        return "redirect:/" + username + "/categories";
    }

// ============================================================================
// === Section: Transactions
// ============================================================================

    /*
     * POST: Aggiungi nuova transazione
     */
    @PostMapping("/{username}/account/{accountId}/add-transaction")
    @Transactional
    public String addTransaction(
            @PathVariable String username,
            @PathVariable Long accountId,
            @Valid TransactionDTO transactionDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        // 1. Autenticazione e autorizzazione
        User currentUser = getAuthenticatedUserOrRedirect(username, redirectAttributes);
        if (currentUser == null) {
            return "redirect:/login";
        }
        // 2. Validazione form
        if (transactionDTO.getDate() == null)
            transactionDTO.setDate(LocalDate.now());
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.transactionDTO", bindingResult);
            redirectAttributes.addFlashAttribute("transactionDTO", transactionDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the errors in the form.");
            return "redirect:/" + username + "/account/" + accountId;
        }
        // 3. Recupera account e verifica possesso
        FinancialAccount account = financialAccountService.getFinancialAccountById(accountId);
        if (!isAccountOwnedByUser(account, currentUser, redirectAttributes, username)) {
            return "redirect:/" + username + "/dashboard";
        }
        // 5. Creazione transazione delegata al service
        Transaction newTransaction = transactionService.createTransaction(
                transactionDTO, account, currentUser, bindingResult);

        // 6. Se ci sono errori o la creazione fallisce, torna al form
        if (bindingResult.hasErrors() || newTransaction == null) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.transactionDTO", bindingResult);
            redirectAttributes.addFlashAttribute("transactionDTO", transactionDTO);
            if (!bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Failed to create transaction.");
            }
            return "redirect:/" + username + "/account/" + accountId;
        }
        redirectAttributes.addFlashAttribute("successMessage", "Transaction added successfully!");
        return "redirect:/" + username + "/account/" + accountId;
    }

    @GetMapping("/{username}/account/{accountId}/recurring")
    public String showRecurringTransactions(
            @PathVariable String username,
            @PathVariable Long accountId,
            Model model) {

        Optional<FinancialAccount> accountOpt = financialAccountService.findById(accountId);

        if (accountOpt.isEmpty()) {
            model.addAttribute("errorMessage", "Account not found.");
            return "redirect:/" + username + "/dashboard";
        }

        FinancialAccount account = accountOpt.get();
        Set<Transaction> all = account.getTransactions();

        List<Transaction> recurring = all.stream()
                .filter(tx -> tx.getRecurrence() != RecurrencePattern.UNA_TANTUM)
                .toList();

        model.addAttribute("account", account);
        model.addAttribute("transactions", recurring);
        model.addAttribute("username", username);

        return "recurring-transactions";
    }

    @PostMapping("/{username}/account/{accountId}/delete-transaction/{transactionId}")
    public String deleteTransaction(
            @PathVariable String username,
            @PathVariable Long accountId,
            @PathVariable Long transactionId,
            RedirectAttributes redirectAttributes) {

        User currentUser = getAuthenticatedUserOrRedirect(username, redirectAttributes);
        if (currentUser == null) {
            return "redirect:/login";
        }

        FinancialAccount account = financialAccountService.getFinancialAccountById(accountId);
        if (!isAccountOwnedByUser(account, currentUser, redirectAttributes, username)) {
            return "redirect:/" + username + "/dashboard";
        }

        Optional<Transaction> transactionOpt = transactionService.findById(transactionId);
        if (transactionOpt.isEmpty() || !transactionOpt.get().getFinancialAccount().equals(account)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Transaction not found or unauthorized.");
            return "redirect:/" + username + "/account/" + accountId;
        }

        transactionService.deleteTransaction(transactionId);
        redirectAttributes.addFlashAttribute("successMessage", "Transaction deleted successfully.");
        return "redirect:/" + username + "/account/" + accountId;
    }

    @GetMapping("/{username}/account/{accountId}/edit-transaction/{transactionId}")
    public String editTransactionForm(
            @PathVariable String username,
            @PathVariable Long accountId,
            @PathVariable Long transactionId,
            Model model,
            RedirectAttributes redirectAttributes) {
        User currentUser = getAuthenticatedUserOrRedirect(username, redirectAttributes);
        if (currentUser == null) {
            return "redirect:/login";
        }
        FinancialAccount account = financialAccountService.getFinancialAccountById(accountId);
        if (!isAccountOwnedByUser(account, currentUser, redirectAttributes, username)) {
            return "redirect:/" + username + "/dashboard";
        }
        Optional<Transaction> transactionOpt = transactionService.findById(transactionId);
        if (transactionOpt.isEmpty() || !transactionOpt.get().getFinancialAccount().equals(account)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Transaction not found or unauthorized.");
            return "redirect:/" + username + "/account/" + accountId;
        }
        Transaction transaction = transactionOpt.get();
        TransactionDTO transactionDTO = new TransactionDTO();
        transactionDTO.setAmount(transaction.getAmount());
        transactionDTO.setDescription(transaction.getDescription());
        transactionDTO.setDate(transaction.getStartDate());
        transactionDTO.setRecurrencePattern(transaction.getRecurrence());

        if (transaction.getMerchant() != null) {
            transactionDTO.setMerchantId(transaction.getMerchant().getId());
        }
        if (transaction.getCategory() != null) {
            transactionDTO.setCategoryId(transaction.getCategory().getId());
        }

        model.addAttribute("transaction", transaction);
        model.addAttribute("transactionDTO", transactionDTO);
        model.addAttribute("account", account);
        model.addAttribute("username", username);
        model.addAttribute("merchants", merchantService.findAllByUser(currentUser));
        model.addAttribute("categories", categoryService.findAllByUser(currentUser));
        return "edit-transaction";
    }

    @PostMapping("/{username}/account/{accountId}/edit-transaction/{transactionId}")
    public String editTransaction(
            @PathVariable String username,
            @PathVariable Long accountId,
            @PathVariable Long transactionId,
            @ModelAttribute("transactionDTO") @Valid TransactionDTO transactionDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        User currentUser = getAuthenticatedUserOrRedirect(username, redirectAttributes);
        if (currentUser == null) {
            return "redirect:/login";
        }

        FinancialAccount account = financialAccountService.getFinancialAccountById(accountId);
        if (!isAccountOwnedByUser(account, currentUser, redirectAttributes, username)) {
            return "redirect:/" + username + "/dashboard";
        }

        Optional<Transaction> transactionOpt = transactionService.findById(transactionId);
        if (transactionOpt.isEmpty() || !transactionOpt.get().getFinancialAccount().equals(account)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Transaction not found or unauthorized.");
            return "redirect:/" + username + "/account/" + accountId;
        }

        Transaction transaction = transactionOpt.get();

        if (bindingResult.hasErrors()) {
            model.addAttribute("transaction", transaction);
            model.addAttribute("account", account);
            model.addAttribute("username", username);
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("merchants", merchantService.findAllByUser(currentUser));
            model.addAttribute("categories", categoryService.findAllByUser(currentUser));
            return "edit-transaction";
        }

        transactionService.updateTransaction(transaction, transactionDTO, transaction.getAmount(), currentUser, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("transaction", transaction);
            model.addAttribute("account", account);
            model.addAttribute("username", username);
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("merchants", merchantService.findAllByUser(currentUser));
            model.addAttribute("categories", categoryService.findAllByUser(currentUser));
            return "edit-transaction";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Transazione aggiornata con successo.");
        return "redirect:/" + username + "/account/" + accountId;
    }

// ============================================================================
// === Section: Budget
// ============================================================================

    /*
     * POST: Aggiungi nuovo budget
     */
    @PostMapping("/{username}/account/{accountId}/add-budget")
    @Transactional
    public String addBudget(
            @PathVariable String username,
            @PathVariable Long accountId,
            @Valid BudgetDTO budgetDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        // 1. Autenticazione e autorizzazione
        User currentUser = getAuthenticatedUserOrRedirect(username, redirectAttributes);
        if (currentUser == null) {
            return "redirect:/login";
        }
        // 2. Validazione form
        if (budgetDTO.getDate() == null)
            budgetDTO.setDate(LocalDate.now());
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute(
                    "org.springframework.validation.BindingResult.budgetDTO", bindingResult);
            redirectAttributes.addFlashAttribute("budgetDTO", budgetDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Please correct the errors in the form.");
            return "redirect:/" + username + "/account/" + accountId;
        }
        // 3. Recupera account e verifica possesso
        FinancialAccount account = financialAccountService.getFinancialAccountById(accountId);
        if (!isAccountOwnedByUser(account, currentUser, redirectAttributes, username)) {
            return "redirect:/" + username + "/dashboard";
        }
        // 5. Creazione budget delegata al service
        Budget newBudget = budgetService.createBudget(budgetDTO, account);

        // 6. Se ci sono errori o la creazione fallisce, torna al form
        if (newBudget == null) {
            redirectAttributes.addFlashAttribute("budgetDTO", budgetDTO);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create budget.");
            return "redirect:/" + username + "/account/" + accountId;
        }
        redirectAttributes.addFlashAttribute("successMessage", "Budget added successfully!");
        return "redirect:/" + username + "/account/" + accountId;
    }

    @PostMapping("/{username}/dashboard/delete-account/{accountId}")
    public String deleteAccount(@PathVariable String username, @PathVariable Long accountId,
            RedirectAttributes redirectAttributes) {
        try {
            financialAccountService.deleteAccount(accountId);
            redirectAttributes.addFlashAttribute("successMessage", "Account eliminato con successo.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore: " + e.getMessage());
        }
        return "redirect:/" + username + "/dashboard";
    }

    @PostMapping("/{username}/account/{accountId}/delete-budget/{budgetId}")
    public String deleteBudget(@PathVariable String username, @PathVariable Long accountId, @PathVariable Long budgetId,
            RedirectAttributes redirectAttributes) {

        try {
            budgetService.deleteBudget(budgetId);
            redirectAttributes.addFlashAttribute("successMessage", "Budget eliminato con successo.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Errore: " + e.getMessage());
        }
        return "redirect:/" + username + "/account/" + accountId;
    }

    /*
     * METODI AUSILIARI
     */

    /**
     * Verifica che l'utente autenticato corrisponda a username;
     * in caso negativo o se non autenticato, imposta un messaggio di errore e
     * ritorna null.
     */
    private User getAuthenticatedUserOrRedirect(String username, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please log in first.");
            return null;
        }
        String logged = authentication.getName();
        if (!logged.equals(username)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized access.");
            return null;
        }
        return userService.getUserByUsername(username);
    }

    /**
     * Controlla che l'account esista e appartenga all'utente corrente.
     * Se non valido, aggiunge flashMessage e ritorna false.
     */
    private boolean isAccountOwnedByUser(
            FinancialAccount account,
            User user,
            RedirectAttributes redirectAttributes,
            String username) {

        if (account == null || !account.getUser().equals(user)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Account not found or unauthorized.");
            return false;
        }
        return true;
    }
}
