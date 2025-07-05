package it.uniroma3.cashlytics.Controller;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import it.uniroma3.cashlytics.DTO.TransactionDTO;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.Transaction;
import it.uniroma3.cashlytics.Model.User;
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
    private TransactionService transactionService;
    @Autowired
    private UserService userService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private MerchantService merchantService;

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
        Set<Transaction> transactions = financialAccountService.getAllTransactionsByAccountId(accountId);
        model.addAttribute("account", account);
        model.addAttribute("username", username);
        model.addAttribute("transactions", transactions);

        // 4. Elenchi di categorie e merchant per l'utente
        model.addAttribute("categories", categoryService.findAllByUser(currentUser));
        model.addAttribute("merchants", merchantService.findAllByUser(currentUser));

        // 5. Tipi di transazione (enum o valori di default)
        model.addAttribute("transactionTypes", loadTransactionTypes());

        // 6. DTO per il form (solo se non già presente)
        if (!model.containsAttribute("transactionDTO")) {
            model.addAttribute("transactionDTO", new TransactionDTO());
        }

        return "accountDetails";
    }

    /*
     * ===============================
     * POST: Aggiungi nuova transazione
     * ===============================
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

        // 4. Controllo importo
        if (transactionDTO.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute("errorMessage", "Transaction amount must be greater than zero.");
            return "redirect:/" + username + "/account/" + accountId;
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

        // 7. Successo
        redirectAttributes.addFlashAttribute("successMessage", "Transaction added successfully!");
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

    /**
     * Restituisce la lista degli enum TransactionType oppure valori base se non
     * esiste l'enum.
     */
    private List<Object> loadTransactionTypes() {
        try {
            Class<?> transactionTypeClass = Class.forName("it.uniroma3.cashlytics.Model.Enums.TransactionType");
            if (transactionTypeClass.isEnum()) {
                return Arrays.asList(transactionTypeClass.getEnumConstants());
            }
        } catch (ClassNotFoundException ignored) {
        }
        return Arrays.asList("INCOME", "EXPENSE", "TRANSFER");
    }

}
