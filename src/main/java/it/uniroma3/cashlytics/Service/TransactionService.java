// TransactionService.java - Aggiornato con gestione Merchant
package it.uniroma3.cashlytics.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.cashlytics.DTO.TransactionDTO;
import it.uniroma3.cashlytics.Exceptions.ResourceNotFoundException;
import it.uniroma3.cashlytics.Exceptions.UnauthorizedAccessException;
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
    private UserService userService;

    public Optional<Transaction> findById(Long transactionId) {
        return transactionRepository.findById(transactionId);
    }

    public Transaction createTransaction(TransactionDTO transactionDTO, FinancialAccount account, User user) {
        // Risolvi merchant
        Merchant merchant = resolveOrCreateMerchant(transactionDTO, user);
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

        if (recurrence != RecurrencePattern.UNA_TANTUM) {
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
            User user) {
        // Risolvi merchant per l'aggiornamento
        Merchant merchant = resolveOrCreateMerchant(transactionDTO, user);
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

    /**
     * Verifica se una transazione appartiene a un utente specifico (per Spring
     * Security)
     */
    public boolean isTransactionOwnedByUser(Long transactionId, String username) {
        try {
            Transaction transaction = findById(transactionId)
                    .orElse(null);
            if (transaction == null) {
                return false;
            }
            User user = userService.getUserByUsername(username);
            return transaction.getFinancialAccount().getUser().equals(user);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Recupera una transazione verificando che appartenga all'utente
     */
    @Transactional(readOnly = true)
    public Transaction getTransactionByUsername(Long transactionId, String username) {
        Transaction transaction = findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException(transactionId));

        User user = userService.getUserByUsername(username);
        if (!transaction.getFinancialAccount().getUser().equals(user)) {
            throw new UnauthorizedAccessException("transaction", transactionId);
        }
        return transaction;
    }

    /**
     * Converte una Transaction in TransactionDTO
     */
    public TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setAmount(transaction.getAmount());
        dto.setDescription(transaction.getDescription());
        dto.setDate(transaction.getStartDate());
        dto.setRecurrencePattern(transaction.getRecurrence());
        if (transaction.getMerchant() != null) {
            dto.setMerchantId(transaction.getMerchant().getId());
        }
        return dto;
    }

    private Merchant resolveOrCreateMerchant(TransactionDTO dto, User user) {
        Long merId = dto.getMerchantId();
        String merName = dto.getMerchantName() != null ? dto.getMerchantName().trim() : "";
        if (merId != null) {
            Optional<Merchant> opt = merchantService.findByIdAndUser(merId, user);
            if (opt.isPresent()) {
                return opt.get();
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
        return null;
    }
}
