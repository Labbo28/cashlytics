package it.uniroma3.cashlytics.Service;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

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

    public Budget createBudget(BudgetDTO budgetDTO, FinancialAccount account,
            User user, BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            return null;

        // 2. Gestione ricorrenza
        RecurrencePattern recurrence = budgetDTO.getRecurrencePattern();
        if (recurrence == null) {
            recurrence = RecurrencePattern.UNA_TANTUM;
        }
        // 3. Gestione data
        LocalDateTime dateTime = budgetDTO.getDate() != null
                ? budgetDTO.getDate().atStartOfDay()
                : LocalDateTime.now();

        // 4. Costruisci budget
        Budget newBudget = new Budget();
        newBudget.setAmount(budgetDTO.getAmount());
        newBudget.setDescription(budgetDTO.getDescription());
        newBudget.setDate(dateTime);
        newBudget.setRecurrence(recurrence);
        newBudget.setFinancialAccount(account);

        // 5. Aggiorna lista budget account
        account.getBudgets().add(newBudget);
        // 6. Aggiorna saldo (sottrai)
        account.setBalance(account.getBalance().subtract(budgetDTO.getAmount()));
        // 7. Salva budget
        return budgetRepository.save(newBudget);
    }

    public void deleteBudget(Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + budgetId));
        budgetRepository.delete(budget);
    }
    /*
     * private Category resolveOrCreateCategory(
     * BudgetDTO dto,
     * User user,
     * BindingResult bindingResult) {
     * Long catId = dto.getCategoryId();
     * String catName = dto.getCategoryName() != null ? dto.getCategoryName().trim()
     * : "";
     * if (catId != null) {
     * Optional<Category> opt = categoryService.findByIdAndUser(catId, user);
     * if (opt.isPresent()) {
     * return opt.get();
     * } else {
     * bindingResult.rejectValue(
     * "categoryName",
     * "error.budgetDTO",
     * "Invalid category selected.");
     * return null;
     * }
     * }
     * if (!catName.isEmpty()) {
     * Optional<Category> optByName = categoryService.findByNameAndUser(catName,
     * user);
     * if (optByName.isPresent()) {
     * return optByName.get();
     * } else {
     * Category newCat = new Category();
     * newCat.setName(catName);
     * newCat.setUser(user);
     * return categoryService.save(newCat);
     * }
     * }
     * bindingResult.rejectValue(
     * "categoryName",
     * "error.budgetDTO",
     * "Category is required.");
     * return null;
     * }
     * 
     * private Merchant resolveOrCreateMerchant(
     * BudgetDTO dto,
     * User user,
     * BindingResult bindingResult) {
     * Long merId = dto.getMerchantId();
     * String merName = dto.getMerchantName() != null ? dto.getMerchantName().trim()
     * : "";
     * if (merId != null) {
     * Optional<Merchant> opt = merchantService.findByIdAndUser(merId, user);
     * if (opt.isPresent()) {
     * return opt.get();
     * } else {
     * bindingResult.rejectValue(
     * "merchantName",
     * "error.budgetDTO",
     * "Invalid merchant selected.");
     * return null;
     * }
     * }
     * if (!merName.isEmpty()) {
     * Optional<Merchant> optByName = merchantService.findByNameAndUser(merName,
     * user);
     * if (optByName.isPresent()) {
     * return optByName.get();
     * } else {
     * Merchant newMer = new Merchant();
     * newMer.setName(merName);
     * newMer.setUser(user);
     * return merchantService.save(newMer);
     * }
     * }
     * bindingResult.rejectValue(
     * "merchantName",
     * "error.budgetDTO",
     * "Merchant is required.");
     * return null;
     * }
     */
}
