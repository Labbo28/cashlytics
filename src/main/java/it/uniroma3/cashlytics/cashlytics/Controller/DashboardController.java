package it.uniroma3.cashlytics.cashlytics.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.cashlytics.cashlytics.Model.User;
import it.uniroma3.cashlytics.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.cashlytics.Model.Enums.AccountType;
import it.uniroma3.cashlytics.cashlytics.service.FinancialAccountService;
import it.uniroma3.cashlytics.cashlytics.service.UserService;
import it.uniroma3.cashlytics.cashlytics.DTO.FinancialAccountDTO;
import it.uniroma3.cashlytics.cashlytics.DTO.TransactionDTO;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class DashboardController {

    @Autowired
    private UserService userService;

    @Autowired
    private FinancialAccountService financialAccountService;
    
    @GetMapping("/{username}/dashboard")
    @Transactional(readOnly = true)
    public String getUserDashboard(@PathVariable String username, Model model) {
        try {
            // Ottieni l'autenticazione corrente
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Debug logging
            System.out.println("Current authentication: " + (authentication != null ? authentication.getName() : "null"));
            System.out.println("Requested username: " + username);
            System.out.println("Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
            
            // Verifica che l'utente sia autenticato
            if (authentication == null || !authentication.isAuthenticated()) {
                System.err.println("User not authenticated, redirecting to login");
                return "redirect:/login";
            }
            
            // Verifica che l'username corrisponda (prevenzione accesso non autorizzato)
            if (!authentication.getName().equals(username)) {
                System.err.println("Username mismatch: " + authentication.getName() + " vs " + username);
                return "redirect:/login";
            }
            
            // Recupera l'utente dal database
            User user = userService.getUserByUsername(username);
            if (user == null) {
                System.err.println("User not found in database: " + username);
                return "redirect:/login";
            }
            
            List<FinancialAccount> financialAccountsList =
                financialAccountService.getAllFinancialAccountByUsername(username);
            
            System.out.println("Loaded " + financialAccountsList.size() + " financial accounts for user: " + username);
            
            // Calcola il bilancio totale
            BigDecimal totalBalance = financialAccountsList.stream()
                .map(FinancialAccount::getBalance)
                .filter(balance -> balance != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Aggiungi dati al modello
            model.addAttribute("user", user);
            model.addAttribute("username", username);
            model.addAttribute("financialAccounts", financialAccountsList);
            model.addAttribute("totalBalance", totalBalance);
            model.addAttribute("accountTypes", AccountType.values());
            
            // Aggiungi un DTO vuoto per il form di creazione nuovo account
            if (!model.containsAttribute("financialAccountDTO")) {
                model.addAttribute("financialAccountDTO", new FinancialAccountDTO());
            }
            
            System.out.println("Dashboard loaded successfully for user: " + username);
            System.out.println("Total balance calculated: " + totalBalance);
            System.out.println("Number of accounts: " + financialAccountsList.size());
            
            return "dashboard";
        } catch (Exception e) {
            System.err.println("Error retrieving user dashboard: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login?error=dashboard_error";
        }
    } // ← qui finisce getUserDashboard

    @PostMapping("/{username}/dashboard/add-account")
    @Transactional
    public String addFinancialAccount(@PathVariable String username,
                                      @Valid FinancialAccountDTO financialAccountDTO,
                                      BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes) {
        try {
            // Ottieni l'autenticazione corrente
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Verifica autenticazione
            if (authentication == null || !authentication.isAuthenticated()) {
                System.err.println("User not authenticated during account creation");
                return "redirect:/login";
            }
            
            if (!authentication.getName().equals(username)) {
                System.err.println("Username mismatch during account creation: " + authentication.getName() + " vs " + username);
                return "redirect:/login";
            }
            
            // Verifica errori di validazione
            if (bindingResult.hasErrors()) {
                System.err.println("Validation errors in account creation");
                bindingResult.getAllErrors().forEach(error -> 
                    System.err.println("Validation error: " + error.getDefaultMessage()));
                
                redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.financialAccountDTO", bindingResult);
                redirectAttributes.addFlashAttribute("financialAccountDTO", financialAccountDTO);
                redirectAttributes.addFlashAttribute("errorMessage", "Please correct the errors in the form.");
                return "redirect:/" + username + "/dashboard";
            }
            
            // Recupera l'utente
            User user = userService.getUserByUsername(username);
            FinancialAccount newAccount = financialAccountService.createFinancialAccount(financialAccountDTO, user);
            // (eventuali altre logiche dopo la creazione)
        } catch (Exception e) {
            System.err.println("Error creating financial account: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error creating account. Please try again.");
        }
        
        return "redirect:/" + username + "/dashboard";
    }

    @GetMapping("/{username}/account/{accountId}")
    public String getAccountDetails(@PathVariable String username,
                                    @PathVariable Long accountId, 
                                    Model model, 
                                    RedirectAttributes redirectAttributes) {
        try {
            // Ottieni l'autenticazione corrente
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Verifica autenticazione
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }
            
            // Carica le transazioni (supponendo che il metodo sia già implementato)
            model.addAttribute("transactions", 
                financialAccountService.getAllTransactionsByAccountId(accountId));
            
            return "accountDetails"; // Nome della view per i dettagli dell’account
        } catch (Exception e) {
            System.err.println("Error retrieving account details: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error retrieving account details. Please try again.");
            return "redirect:/" + username + "/dashboard";
        }
    } // ← qui mancava la parentesi di chiusura di getAccountDetails

    @PostMapping("/{username}/account/{accountId}/add-transaction")
    public String addTransaction(@PathVariable String username,
                                 @PathVariable Long accountId,
                                 @Valid TransactionDTO transactionDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        try {
            // Ottieni l'autenticazione corrente
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Verifica autenticazione
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }
            
            if (!authentication.getName().equals(username)) {
                return "redirect:/login";
            }
            
            // Verifica errori di validazione
            if (bindingResult.hasErrors()) {
                redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.transactionDTO", bindingResult);
                redirectAttributes.addFlashAttribute("transactionDTO", transactionDTO);
                redirectAttributes.addFlashAttribute("errorMessage", "Please correct the errors in the form.");
                return "redirect:/" + username + "/account/" + accountId;
            }

            // TODO: Implement transaction creation logic qui

            return "redirect:/" + username + "/account/" + accountId;
        } catch (Exception e) {
            System.err.println("Error adding transaction: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error adding transaction. Please try again.");
            return "redirect:/" + username + "/account/" + accountId;
        }
    }

} 
