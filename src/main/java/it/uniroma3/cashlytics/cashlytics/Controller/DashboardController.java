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
import it.uniroma3.cashlytics.cashlytics.Repository.UserRepository;
import it.uniroma3.cashlytics.cashlytics.Repository.FinancialAccountRepository;
import it.uniroma3.cashlytics.cashlytics.DTO.FinancialAccountDTO;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

@Controller
public class DashboardController {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private FinancialAccountRepository financialAccountRepository;
    
    /**
     * Displays the user's personal dashboard with financial accounts.
     */
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
            User user = userRepository.findByCredentials_Username(username)
                .orElse(null);
            
            if (user == null) {
                System.err.println("User not found in database: " + username);
                return "redirect:/login";
            }
            
            // SOLUZIONE MIGLIORATA: Recupera gli account finanziari direttamente dal repository
            // Questo evita problemi di lazy loading e assicura dati freschi
            List<FinancialAccount> financialAccountsList = 
                financialAccountRepository.findByUser_Credentials_Username(username);
            
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
    }
    
    /**
     * Handles the creation of a new financial account.
     */
    @PostMapping("/{username}/dashboard/add-account")
    @Transactional
    public String addFinancialAccount(@PathVariable String username,
                                    @Valid FinancialAccountDTO financialAccountDTO,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        
        try {
            // Ottieni l'autenticazione corrente
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Debug logging
            System.out.println("Add account - Current authentication: " + (authentication != null ? authentication.getName() : "null"));
            System.out.println("Add account - Requested username: " + username);
            System.out.println("Add account - Is authenticated: " + (authentication != null && authentication.isAuthenticated()));
            
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
            User user = userRepository.findByCredentials_Username(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            // Debug: stampa i dati ricevuti
            System.out.println("Creating account with data:");
            System.out.println("- Account Type: " + financialAccountDTO.getAccountType());
            System.out.println("- Balance: " + financialAccountDTO.getBalance());
            System.out.println("- User ID: " + user.getId());
            
            // Crea nuovo account finanziario
            FinancialAccount newAccount = new FinancialAccount();
            newAccount.setAccountType(financialAccountDTO.getAccountType());
            newAccount.setBalance(financialAccountDTO.getBalance() != null ? 
                financialAccountDTO.getBalance() : BigDecimal.ZERO);
            newAccount.setUser(user);
            
            // Salva l'account e forza il flush per assicurarsi che venga scritto nel DB
            FinancialAccount savedAccount = financialAccountRepository.save(newAccount);
            financialAccountRepository.flush(); // Forza la scrittura immediata nel database
            
            System.out.println("Account created successfully with ID: " + savedAccount.getId());
            System.out.println("Account balance: " + savedAccount.getBalance());
            System.out.println("Account type: " + savedAccount.getAccountType());
            
            // Verifica che l'account sia stato salvato correttamente
            List<FinancialAccount> userAccounts = financialAccountRepository.findByUser_Credentials_Username(username);
            System.out.println("Total accounts for user after creation: " + userAccounts.size());
            
            redirectAttributes.addFlashAttribute("successMessage", 
                "Account " + financialAccountDTO.getAccountType() + " created successfully!");
            
        } catch (Exception e) {
            System.err.println("Error creating financial account: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error creating account. Please try again.");
        }
        
        return "redirect:/" + username + "/dashboard";
    }
}