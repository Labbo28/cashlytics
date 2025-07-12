package it.uniroma3.cashlytics.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import it.uniroma3.cashlytics.DTO.TransactionDTO;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.Transaction;
import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import it.uniroma3.cashlytics.Model.Enums.TransactionType;
import it.uniroma3.cashlytics.Repository.TransactionRepository;
import jakarta.validation.Valid;

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

    public Optional<Transaction> findById(Long transactionId) {
        return transactionRepository.findById(transactionId);
    }

    public void deleteTransaction(Long transactionId) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isPresent()) {
            Transaction transaction = transactionOpt.get();
            FinancialAccount account = transaction.getFinancialAccount();
            // Aggiorna il saldo dell'account
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            // Rimuovi la transazione dall'account
            account.getTransactions().remove(transaction);
            // Elimina la transazione dal repository
            transactionRepository.delete(transaction);
        } else {
            throw new IllegalArgumentException("Transaction with ID " + transactionId + " does not exist.");
        }
    }

    public void updateTransaction(Transaction transaction, TransactionDTO transactionDTO, BigDecimal oldAmount) {
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setDescription(transactionDTO.getDescription());
        transaction.setStartDate(transactionDTO.getDate());
        transaction.setRecurrence(transactionDTO.getRecurrencePattern());
        FinancialAccount account = transaction.getFinancialAccount();
        // Aggiorna il saldo dell'account
        account.setBalance(account.getBalance().subtract(oldAmount).add(transactionDTO.getAmount()));
        // Salva le modifiche
        transactionRepository.save(transaction);
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


