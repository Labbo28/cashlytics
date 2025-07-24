package it.uniroma3.cashlytics.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import it.uniroma3.cashlytics.DTO.TransactionDTO;
import it.uniroma3.cashlytics.Model.Budget;
import it.uniroma3.cashlytics.Model.Category;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.Merchant;
import it.uniroma3.cashlytics.Model.Transaction;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import it.uniroma3.cashlytics.Model.Enums.TransactionType;
import it.uniroma3.cashlytics.Repository.BudgetRepository;
import it.uniroma3.cashlytics.Repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private MerchantService merchantService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BudgetRepository budgetRepository;
    @Autowired
    private EntityManager entityManager;

    public Optional<Transaction> findById(Long transactionId) {
        return transactionRepository.findById(transactionId);
    }

    public Transaction createTransaction(TransactionDTO transactionDTO,
            FinancialAccount account, User user, BindingResult bindingResult, AtomicBoolean budgetUpdated) {
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

        // If expense and has category, subtract from matching budget
        if (!isIncome && category != null) {
            Budget budget = budgetRepository.findByFinancialAccountAndCategory(account, category);
            if (budget != null) {
                // Per le spese (amount negativo), sottrai l'importo assoluto dal budget
                BigDecimal expenseAmount = transactionDTO.getAmount().abs();
                budget.setAmount(budget.getAmount().subtract(expenseAmount));
                budgetRepository.save(budget);
                budgetUpdated.set(true); // Indica che è stato aggiornato un budget
            }
        }

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
        Category oldCategory = transaction.getCategory();

        // 1. Se la vecchia transazione era una spesa con categoria, ripristina il
        // budget
        if (oldAmount.signum() < 0 && oldCategory != null) {
            Budget oldBudget = budgetRepository.findByFinancialAccountAndCategory(account, oldCategory);
            if (oldBudget != null) {
                oldBudget.setAmount(oldBudget.getAmount().add(oldAmount.abs()));
                budgetRepository.save(oldBudget);
            }
        }

        // 2. Se la nuova transazione è una spesa con categoria, sottrai dal budget
        if (!isIncome && category != null) {
            Budget newBudget = budgetRepository.findByFinancialAccountAndCategory(account, category);
            if (newBudget != null) {
                BigDecimal expenseAmount = transactionDTO.getAmount().abs();
                newBudget.setAmount(newBudget.getAmount().subtract(expenseAmount));
                budgetRepository.save(newBudget);
            }
        }
        transactionRepository.save(transaction);
    }

    public List<Transaction> filterTransactions(
            Long accountId,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            String transactionType,
            LocalDate startDate,
            LocalDate endDate,
            String merchantId,
            String description,
            boolean onlyRecurring,
            Long categoryId,
            String orderBy) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Transaction> cq = cb.createQuery(Transaction.class);
        Root<Transaction> root = cq.from(Transaction.class);

        List<Predicate> predicates = new ArrayList<>();

        // Filtro per account
        predicates.add(cb.equal(root.get("financialAccount").get("id"), accountId));

        if (minAmount != null) {
            predicates.add(cb.ge(root.get("amount"), minAmount));
        }
        if (maxAmount != null) {
            predicates.add(cb.le(root.get("amount"), maxAmount));
        }
        if (transactionType != null && !transactionType.isEmpty()) {
            predicates.add(cb.equal(root.get("transactionType"), TransactionType.valueOf(transactionType)));
        }
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("date"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("date"), endDate));
        }
        if (merchantId != null && !merchantId.isEmpty()) {
            predicates.add(cb.equal(root.get("merchant").get("id"), Long.parseLong(merchantId)));
        }
        if (description != null && !description.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("description")), "%" + description.toLowerCase() + "%"));
        }
        if (onlyRecurring) {
            predicates.add(cb.notEqual(root.get("recurrence"), RecurrencePattern.UNA_TANTUM));
        }
        if (categoryId != null) {
            predicates.add(cb.equal(root.get("category").get("id"), categoryId));
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));

        // Order by logic
        if (orderBy == null || orderBy.equals("MOST_RECENT")) {
            cq.orderBy(cb.desc(root.get("date")));
        } else if (orderBy.equals("OLDEST")) {
            cq.orderBy(cb.asc(root.get("date")));
        } else if (orderBy.equals("HIGHEST")) {
            cq.orderBy(cb.desc(root.get("amount")));
        } else if (orderBy.equals("LOWEST")) {
            cq.orderBy(cb.asc(root.get("amount")));
        } else {
            cq.orderBy(cb.desc(root.get("date"))); // default fallback
        }

        return entityManager.createQuery(cq).getResultList();
    }
}
