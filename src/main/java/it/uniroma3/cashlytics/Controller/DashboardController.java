package it.uniroma3.cashlytics.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("#username == authentication.name")
    @GetMapping("/{username}/dashboard")
    public String getUserDashboard(@PathVariable String username, Model model) {
        try {
            // Recupera l'utente dal database
            User user = userService.getUserByUsername(username);
            if (user == null) {
                System.err.println("User not found in database: " + username);
                return "redirect:/login";
            }
            List<FinancialAccount> financialAccountsList = financialAccountService
                    .getAllFinancialAccountsByUsername(username);
            System.out.println("Loaded " + financialAccountsList.size() + " financial accounts for user: " + username);

            // Aggiungi dati al modello
            model.addAttribute("username", username);
            model.addAttribute("financialAccounts", financialAccountsList);
            model.addAttribute("totalBalance", user.getTotalBalance());
            model.addAttribute("accountTypes", AccountType.values());

            // Aggiungi un DTO vuoto per il form di creazione nuovo account
            if (!model.containsAttribute("financialAccountDTO")) {
                model.addAttribute("financialAccountDTO", new FinancialAccountDTO());
            }
            return "dashboard";
        } catch (Exception e) {
            System.err.println("Error retrieving user dashboard: " + e.getMessage());
            return "redirect:/login?error=true";
        }
    }

    @PreAuthorize("#username == authentication.name")
    @PostMapping("/{username}/dashboard/add-account")
    @Transactional
    public String addFinancialAccount(@PathVariable String username,
            @Valid FinancialAccountDTO financialAccountDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        try {
            if (bindingResult.hasErrors()) {
                return "dashboard";
            }
            User user = userService.getUserByUsername(username);
            financialAccountService.createFinancialAccount(financialAccountDTO, user);
        } catch (Exception e) {
            System.err.println("Error creating financial account: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Errore nel creare l'account. Riprova più tardi.");
        }
        return "redirect:/" + username + "/dashboard";
    }

}
