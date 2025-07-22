package it.uniroma3.cashlytics.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import it.uniroma3.cashlytics.DTO.BudgetDTO;
import it.uniroma3.cashlytics.Model.Budget;
import it.uniroma3.cashlytics.Model.Category;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.Merchant;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import it.uniroma3.cashlytics.Repository.BudgetRepository;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private CategoryService categoryService;

    public Optional<Budget> findById(Long budgetId) {
        return budgetRepository.findById(budgetId);
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

        // Enforce category presence
        if (budgetDTO.getCategoryId() == null) {
            throw new IllegalArgumentException("Budget must have a category");
        }
        Category category = categoryService.findById(budgetDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category for budget"));

        // Costruisci budget
        Budget newBudget = new Budget();
        newBudget.setAmount(budgetDTO.getAmount());
        newBudget.setDescription(budgetDTO.getDescription());
        newBudget.setDate(dateTime);
        newBudget.setRecurrence(recurrence);
        newBudget.setFinancialAccount(account);
        newBudget.setCategory(category);

        // Aggiorna lista budget account
        account.getBudgets().add(newBudget);
        // (No longer subtract from account balance)

        return budgetRepository.save(newBudget);
    }

    // SOSTITUISCI tutto il metodo:
public void deleteBudget(Long budgetId, FinancialAccount account) {
    Budget budget = budgetRepository.findById(budgetId)
            .orElseThrow(() -> new RuntimeException("Budget not found with id: " + budgetId));
    
    // Rimuovi il budget dalla lista dell'account
    account.getBudgets().remove(budget);
    
    // Elimina il budget (non modificare il saldo dell'account)
    budgetRepository.delete(budget);
}
}
