package it.uniroma3.cashlytics.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import it.uniroma3.cashlytics.DTO.TransactionDTO;
import it.uniroma3.cashlytics.Model.Category;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.Merchant;
import it.uniroma3.cashlytics.Model.Transaction;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import it.uniroma3.cashlytics.Model.Enums.TransactionType;
import it.uniroma3.cashlytics.Repository.TransactionRepository;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private CategoryService categoryService;

    public Optional<Transaction> findById(Long transactionId) {
        return transactionRepository.findById(transactionId);
    }

    public Transaction createTransaction(TransactionDTO transactionDTO,
            FinancialAccount account, User user, BindingResult bindingResult) {
        // Risolvi merchant e category (possono essere null)
        Merchant merchant = merchantService.resolveOrCreateMerchant(transactionDTO, user, bindingResult);
        Category category = categoryService.resolveOrCreateCategory(transactionDTO, user, bindingResult);
        // Se ci sono errori nella risoluzione merchant o category, interrompi
        if (bindingResult.hasErrors())
            return null;

        // Determina tipo di transazione dall'importo
        boolean isIncome = transactionDTO.getAmount().signum() >= 0;
        TransactionType type = isIncome ? TransactionType.INCOME : TransactionType.EXPENSE;
        // Gestione ricorrenza
        RecurrencePattern recurrence = transactionDTO.getRecurrencePattern();
        if (recurrence == null) {
            recurrence = RecurrencePattern.UNA_TANTUM;
        }
        // Gestione data
        LocalDateTime dateTime = transactionDTO.getDate() != null
                ? transactionDTO.getDate().atStartOfDay()
                : LocalDateTime.now();

        // Costruisci transazione
        Transaction newTransaction = new Transaction();
        newTransaction.setAmount(transactionDTO.getAmount());
        newTransaction.setDescription(transactionDTO.getDescription());
        newTransaction.setTransactionType(type);
        newTransaction.setDate(dateTime);
        newTransaction.setRecurrence(recurrence);
        newTransaction.setFinancialAccount(account);
        newTransaction.setMerchant(merchant); // Può essere null
        newTransaction.setCategory(category); // Può essere null
        newTransaction.setRecurring(recurrence != RecurrencePattern.UNA_TANTUM);

        // Aggiorna saldo
        account.getTransactions().add(newTransaction);
        account.setBalance(account.getBalance().add(transactionDTO.getAmount()));

        return transactionRepository.save(newTransaction);
    }

    public void deleteTransaction(Long transactionId) {
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isPresent()) {
            Transaction transaction = transactionOpt.get();
            FinancialAccount account = transaction.getFinancialAccount();
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            account.getTransactions().remove(transaction);
            transactionRepository.delete(transaction);
        } else {
            throw new IllegalArgumentException("Transaction with ID " + transactionId + " does not exist.");
        }
    }

    public void updateTransaction(Transaction transaction, TransactionDTO transactionDTO, BigDecimal oldAmount,
            User user, BindingResult bindingResult) {
        // Risolvi merchant e category (possono essere null)
        Merchant merchant = merchantService.resolveOrCreateMerchant(transactionDTO, user, bindingResult);
        Category category = categoryService.resolveOrCreateCategory(transactionDTO, user, bindingResult);
        // Se ci sono errori nella risoluzione merchant o category, interrompi
        if (bindingResult.hasErrors())
            return;

        // Determina tipo di transazione dall'importo
        boolean isIncome = transactionDTO.getAmount().signum() >= 0;
        TransactionType type = isIncome ? TransactionType.INCOME : TransactionType.EXPENSE;
        // Gestione ricorrenza
        RecurrencePattern recurrence = transactionDTO.getRecurrencePattern();
        if (recurrence == null) {
            recurrence = RecurrencePattern.UNA_TANTUM;
        }

        // Aggiorna i campi della transazione
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setDescription(transactionDTO.getDescription());
        transaction.setTransactionType(type);
        transaction.setDate(transactionDTO.getDate().atStartOfDay());
        transaction.setRecurrence(recurrence);
        transaction.setMerchant(merchant); // Può essere null
        transaction.setCategory(category); // Può essere null
        transaction.setRecurring(recurrence != RecurrencePattern.UNA_TANTUM);

        // Aggiorna saldo
        FinancialAccount account = transaction.getFinancialAccount();
        account.setBalance(account.getBalance().subtract(oldAmount).add(transactionDTO.getAmount()));

        transactionRepository.save(transaction);
    }

}
