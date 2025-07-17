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

    public Transaction createTransaction(TransactionDTO transactionDTO, FinancialAccount account, User user, BindingResult bindingResult) {

        // Risolvi merchant (può essere null se non specificato)
        Merchant merchant = resolveOrCreateMerchant(transactionDTO, user, bindingResult);

        // Risolvi category (può essere null se non specificato)
        Category category = resolveOrCreateCategory(transactionDTO, user, bindingResult);

        // Se ci sono errori nella risoluzione merchant o category, interrompi
        if (bindingResult.hasErrors()) {
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
        newTransaction.setMerchant(merchant); // Può essere null
        newTransaction.setCategory(category); // Può essere null

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

        // Risolvi category per l'aggiornamento
        Category category = resolveOrCreateCategory(transactionDTO, user, bindingResult);

        // Se ci sono errori nella risoluzione merchant o category, interrompi
        if (bindingResult.hasErrors()) {
            return;
        }

        // Aggiorna i campi della transazione
        transaction.setAmount(transactionDTO.getAmount());
        transaction.setDescription(transactionDTO.getDescription());
        transaction.setDate(transactionDTO.getDate().atStartOfDay());
        transaction.setMerchant(merchant); // Può essere null
        transaction.setCategory(category); // Può essere null

        // Aggiorna il tipo di transazione basato sull'importo
        boolean isIncome = transactionDTO.getAmount().signum() >= 0;
        TransactionType type = isIncome ? TransactionType.INCOME : TransactionType.EXPENSE;
        transaction.setTransactionType(type);

        // Aggiorna ricorrenza
        RecurrencePattern recurrence = transactionDTO.getRecurrencePattern();
        if (recurrence == null) {
            recurrence = RecurrencePattern.UNA_TANTUM;
        }
        transaction.setRecurrence(recurrence);
        transaction.setRecurring(recurrence != RecurrencePattern.UNA_TANTUM);

        FinancialAccount account = transaction.getFinancialAccount();
        account.setBalance(account.getBalance().subtract(oldAmount).add(transactionDTO.getAmount()));

        transactionRepository.save(transaction);
    }

    private Merchant resolveOrCreateMerchant(TransactionDTO dto, User user, BindingResult bindingResult) {
        Long merId = dto.getMerchantId();
        String merName = dto.getMerchantName() != null ? dto.getMerchantName().trim() : "";

        // Caso 1: ID merchant fornito
        if (merId != null) {
            Optional<Merchant> opt = merchantService.findByIdAndUser(merId, user);
            if (opt.isPresent()) {
                return opt.get();
            } else {
                bindingResult.rejectValue("merchantId", "error.transactionDTO", "Invalid merchant selected.");
                return null;
            }
        }

        // Caso 2: Nome merchant fornito
        if (!merName.isEmpty()) {
            Optional<Merchant> optByName = merchantService.findByNameAndUser(merName, user);
            if (optByName.isPresent()) {
                return optByName.get();
            } else {
                // Crea nuovo merchant
                Merchant newMer = new Merchant();
                newMer.setName(merName);
                newMer.setUser(user);
                return merchantService.save(newMer);
            }
        }

        // Caso 3: Nessun merchant fornito - QUESTO È OK, ritorna null
        return null; // Merchant opzionale
    }

    private Category resolveOrCreateCategory(TransactionDTO dto, User user, BindingResult bindingResult) {
        Long catId = dto.getCategoryId();
        String catName = dto.getCategoryName() != null ? dto.getCategoryName().trim() : "";

        // Caso 1: ID categoria fornito
        if (catId != null) {
            Optional<Category> opt = categoryService.findByIdAndUser(catId, user);
            if (opt.isPresent()) {
                return opt.get();
            } else {
                bindingResult.rejectValue("categoryId", "error.transactionDTO", "Categoria selezionata non valida.");
                return null;
            }
        }

        // Caso 2: Nome categoria fornito
        if (!catName.isEmpty()) {
            Optional<Category> optByName = categoryService.findByNameAndUser(catName, user);
            if (optByName.isPresent()) {
                return optByName.get();
            } else {
                // Crea nuova categoria con valori di default
                Category newCat = new Category();
                newCat.setName(catName);
                newCat.setIcon("https://cdn-icons-png.flaticon.com/512/1077/1077976.png"); // Icona di default
                newCat.setColor("#6C757D"); // Colore grigio di default
                newCat.setUser(user);
                return categoryService.save(newCat);
            }
        }

        // Caso 3: Nessuna categoria fornita - QUESTO È OK, ritorna null
        return null; // Categoria opzionale
    }
}
