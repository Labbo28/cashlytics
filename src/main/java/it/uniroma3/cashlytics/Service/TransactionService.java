package it.uniroma3.cashlytics.Service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import it.uniroma3.cashlytics.DTO.TransactionDTO;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.Transaction;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import it.uniroma3.cashlytics.Model.Enums.TransactionType;
import it.uniroma3.cashlytics.Repository.TransactionRepository;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction createTransaction(TransactionDTO transactionDTO, FinancialAccount account){
        

        // 1. Determina tipo di transazione da amount
        boolean isIncome = transactionDTO.getAmount().signum() >= 0;
        TransactionType type = isIncome ? TransactionType.INCOME : TransactionType.EXPENSE;

        // 2. Gestione ricorrenza
        RecurrencePattern recurrence = transactionDTO.getRecurrencePattern();
        if (recurrence == null) {

            recurrence = RecurrencePattern.UNA_TANTUM;
        }
        // 3. Gestione data
        LocalDateTime dateTime = transactionDTO.getDate() != null
                ? transactionDTO.getDate().atStartOfDay()
                : LocalDateTime.now();

        // 4. Costruisci transazione
        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(transactionDTO.getAmount());
        newTransaction.setDescription(transactionDTO.getDescription());
        newTransaction.setTransactionType(type);
        newTransaction.setDate(dateTime);
        newTransaction.setRecurrence(recurrence);
        newTransaction.setFinancialAccount(account);
        if(recurrence != RecurrencePattern.UNA_TANTUM) {
            newTransaction.setRecurring(true);
        } else {
            newTransaction.setRecurring(false);
        }

        // 5. Aggiorna lista transazioni account
        account.getTransactions().add(newTransaction);
        // 6. Aggiorna saldo (aggiungi o sottrai)
        account.setBalance(account.getBalance().add(transactionDTO.getAmount()));
        // 7. Salva transazione
        return transactionRepository.save(newTransaction);
    }

    /*
     * private Category resolveOrCreateCategory(
     * TransactionDTO dto,
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
     * "error.transactionDTO",
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
     * "error.transactionDTO",
     * "Category is required.");
     * return null;
     * }
     * 
     * private Merchant resolveOrCreateMerchant(
     * TransactionDTO dto,
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
     * "error.transactionDTO",
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
     * "error.transactionDTO",
     * "Merchant is required.");
     * return null;
     * }
     */
}


