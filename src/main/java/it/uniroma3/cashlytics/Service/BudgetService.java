package it.uniroma3.cashlytics.Service;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.cashlytics.DTO.BudgetDTO;
import it.uniroma3.cashlytics.Model.Budget;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import it.uniroma3.cashlytics.Repository.BudgetRepository;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

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

    public void deleteBudget(Long budgetId, FinancialAccount account) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + budgetId));
        // Aggiorna saldo (aggiungi)
        account.setBalance(account.getBalance().subtract(budget.getAmount()));
        budgetRepository.delete(budget);
    }

}