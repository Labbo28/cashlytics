// TransactionService.java - Aggiornato con gestione Merchant
package it.uniroma3.cashlytics.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import it.uniroma3.cashlytics.DTO.TransactionDTO;
import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import it.uniroma3.cashlytics.Model.Enums.TransactionType;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.Merchant;
import it.uniroma3.cashlytics.Model.Transaction;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Repository.TransactionRepository;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MerchantService merchantService;

    public Transaction createTransaction(TransactionDTO transactionDTO, FinancialAccount account, User user, BindingResult bindingResult) {

        // Risolvi merchant
        Merchant merchant = resolveOrCreateMerchant(transactionDTO, user, bindingResult);
        if (merchant == null) {
            return null;
        }

        // Determina tipo di transazione da amount
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
        newTransaction.setMerchant(merchant);

        if(recurrence != RecurrencePattern.UNA_TANTUM) {
            newTransaction.setRecurring(true);
        } else {
            newTransaction.setRecurring(false);
        }

        // Aggiorna lista transazioni account
        account.getTransactions().add(newTransaction);
        // Aggiorna saldo
        account.setBalance(account.getBalance().add(transactionDTO.getAmount()));

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
            account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            account.getTransactions().remove(transaction);
            transactionRepository.delete(transaction);
        } else {
            throw new IllegalArgumentException("Transaction with ID " + transactionId + " does not exist.");
        }
    }

    public void updateTransaction(Transaction transaction, TransactionDTO transactionDTO, BigDecimal oldAmount, User user, BindingResult bindingResult) {
        // Risolvi merchant per l'aggiornamento
        Merchant merchant = resolveOrCreateMerchant(transactionDTO, user, bindingResult);
        if (merchant == null) {
            return;
        }

        transaction.setAmount(transactionDTO.getAmount());
        transaction.setDescription(transactionDTO.getDescription());
        transaction.setStartDate(transactionDTO.getDate());
        transaction.setRecurrence(transactionDTO.getRecurrencePattern());
        transaction.setMerchant(merchant);

        FinancialAccount account = transaction.getFinancialAccount();
        account.setBalance(account.getBalance().subtract(oldAmount).add(transactionDTO.getAmount()));

        transactionRepository.save(transaction);
    }

    private Merchant resolveOrCreateMerchant(TransactionDTO dto, User user, BindingResult bindingResult) {
        Long merId = dto.getMerchantId();
        String merName = dto.getMerchantName() != null ? dto.getMerchantName().trim() : "";

        if (merId != null) {
            Optional<Merchant> opt = merchantService.findByIdAndUser(merId, user);
            if (opt.isPresent()) {
                return opt.get();
            } else {
                bindingResult.rejectValue("merchantName", "error.transactionDTO", "Invalid merchant selected.");
                return null;
            }
        }

        if (!merName.isEmpty()) {
            Optional<Merchant> optByName = merchantService.findByNameAndUser(merName, user);
            if (optByName.isPresent()) {
                return optByName.get();
            } else {
                Merchant newMer = new Merchant();
                newMer.setName(merName);
                newMer.setUser(user);
                return merchantService.save(newMer);
            }
        }

        bindingResult.rejectValue("merchantName", "error.transactionDTO", "Merchant is required.");
        return null;
    }
}
