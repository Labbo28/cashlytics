package it.uniroma3.cashlytics.Controller;

import java.util.List;
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
import it.uniroma3.cashlytics.DTO.FinancialAccountDTO;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Model.Enums.AccountType;
import it.uniroma3.cashlytics.Service.FinancialAccountService;
import it.uniroma3.cashlytics.Service.UserService;
import jakarta.validation.Valid;

@Controller
public class DashboardController {

    @Autowired
    private UserService userService;
    @Autowired
    private FinancialAccountService financialAccountService;
    // @Autowired
    // private TransactionService transactionService;

    @GetMapping("/{username}/dashboard")
    @Transactional(readOnly = true)
    public String getUserDashboard(@PathVariable String username, Model model) {
        try {
            // Ottieni l'autenticazione corrente
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Debug logging
            System.out
                    .println("Current authentication: " + (authentication != null ? authentication.getName() : "null"));
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

            List<FinancialAccount> financialAccountsList = financialAccountService
                    .getAllFinancialAccountByUsername(username);
            System.out.println("Loaded " + financialAccountsList.size() + " financial accounts for user: " + username);

            // Aggiungi dati al modello
            model.addAttribute("user", user);
            model.addAttribute("username", username);
            model.addAttribute("financialAccounts", financialAccountsList);
            model.addAttribute("totalBalance", user.getTotalBalance());
            model.addAttribute("accountTypes", AccountType.values());

            // Aggiungi un DTO vuoto per il form di creazione nuovo account
            if (!model.containsAttribute("financialAccountDTO")) {
                model.addAttribute("financialAccountDTO", new FinancialAccountDTO());
            }

            System.out.println("Dashboard loaded successfully for user: " + username);
            System.out.println("Total balance calculated: " + user.getTotalBalance());
            System.out.println("Number of accounts: " + financialAccountsList.size());
            return "dashboard";
        } catch (Exception e) {
            System.err.println("Error retrieving user dashboard: " + e.getMessage());
            e.printStackTrace();
            return "redirect:/login?error=dashboard_error";
        }
    }

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
                System.err.println(
                        "Username mismatch during account creation: " + authentication.getName() + " vs " + username);
                return "redirect:/login";
            }

            // Verifica errori di validazione
            if (bindingResult.hasErrors()) {
                System.err.println("Validation errors in account creation");
                bindingResult.getAllErrors()
                        .forEach(error -> System.err.println("Validation error: " + error.getDefaultMessage()));

                redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.financialAccountDTO",
                        bindingResult);
                redirectAttributes.addFlashAttribute("financialAccountDTO", financialAccountDTO);
                redirectAttributes.addFlashAttribute("errorMessage", "Please correct the errors in the form.");
                return "redirect:/" + username + "/dashboard";
            }

            // Recupera l'utente
            User user = userService.getUserByUsername(username);
            FinancialAccount newAccount = financialAccountService.createFinancialAccount(financialAccountDTO, user);
            // TODO eventuali altre logiche dopo la creazione
        } catch (Exception e) {
            System.err.println("Error creating financial account: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating account. Please try again.");
        }
        return "redirect:/" + username + "/dashboard";
    }

}
