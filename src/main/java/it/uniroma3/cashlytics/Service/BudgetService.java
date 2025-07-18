package it.uniroma3.cashlytics.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.uniroma3.cashlytics.DTO.BudgetDTO;
import it.uniroma3.cashlytics.Exceptions.ResourceNotFoundException;
import it.uniroma3.cashlytics.Exceptions.UnauthorizedAccessException;
import it.uniroma3.cashlytics.Model.Budget;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import it.uniroma3.cashlytics.Repository.BudgetRepository;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    UserService userService;


    public Optional<Budget> findById(Long transactionId) {
        return budgetRepository.findById(transactionId);
    }


    public Budget createBudget(BudgetDTO budgetDTO, FinancialAccount account, User user) {
        // Gestione ricorrenza
        RecurrencePattern recurrence = budgetDTO.getRecurrencePattern();
        if (recurrence == null) {
            recurrence = RecurrencePattern.UNA_TANTUM;
        }

        // Gestione data
        LocalDateTime dateTime = budgetDTO.getDate() != null
                ? budgetDTO.getDate().atStartOfDay()
                : LocalDateTime.now();

        // Costruisci budget
        Budget newBudget = new Budget();
        newBudget.setAmount(budgetDTO.getAmount());
        newBudget.setDescription(budgetDTO.getDescription());
        newBudget.setDate(dateTime);
        newBudget.setRecurrence(recurrence);
        newBudget.setFinancialAccount(account);

        // Aggiorna lista budget account
        account.getBudgets().add(newBudget);
        // Aggiorna saldo (sottrai)
        account.setBalance(account.getBalance().subtract(budgetDTO.getAmount()));

        return budgetRepository.save(newBudget);
    }

    @Transactional
    public void deleteBudgetForUser(Long budgetId, String username) {
        Budget budget = getBudgetByUsername(budgetId, username);
        budgetRepository.delete(budget);
    }

    /**
     * Verifica se un budget appartiene a un utente specifico (per Spring Security)
     */
    public boolean isBudgetOwnedByUser(Long budgetId, String username) {
        try {
            Budget budget = findById(budgetId)
                    .orElse(null);
            if (budget == null) {
                return false;
            }
            User user = userService.getUserByUsername(username);
            return budget.getFinancialAccount().getUser().equals(user);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Recupera un budget verificando che appartenga all'utente
     */
    @Transactional(readOnly = true)
    public Budget getBudgetByUsername(Long budgetId, String username) {
        Budget budget = findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException(budgetId));

        User user = userService.getUserByUsername(username);
        if (!budget.getFinancialAccount().getUser().equals(user)) {
            throw new UnauthorizedAccessException("budget", budgetId);
        }
        return budget;
    }

}

