package it.uniroma3.cashlytics.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import it.uniroma3.cashlytics.DTO.FinancialAccountDTO;
import it.uniroma3.cashlytics.Exceptions.ResourceNotFoundException;
import it.uniroma3.cashlytics.Exceptions.UnauthorizedAccessException;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.Transaction;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Repository.FinancialAccountRepository;

@Service
public class FinancialAccountService {

    @Autowired
    FinancialAccountRepository financialAccountRepository;
    @Autowired
    private UserService userService;

    public Optional<FinancialAccount> findById(Long accountId) {
        return financialAccountRepository.findById(accountId);
    }

    public List<FinancialAccount> getAllFinancialAccountsByUsername(String username) {
        return financialAccountRepository.findByUser_Credentials_Username(username);
    }

    /**
     * Recupera un account verificando che appartenga all'utente
     */
    @Transactional(readOnly = true)
    public FinancialAccount getFinancialAccountByUsername(Long accountId, String username) {
        FinancialAccount account = findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException(accountId));

        User user = userService.getUserByUsername(username);
        if (!account.getUser().equals(user)) {
            throw new UnauthorizedAccessException("account", accountId);
        }
        return account;
    }

    public FinancialAccount createFinancialAccount(FinancialAccountDTO financialAccountDTO, User user)
            throws IllegalArgumentException {
        if (financialAccountDTO.getAccountType() == null) {
            throw new IllegalArgumentException("AccountType non può essere null.");
        }
        FinancialAccount newAccount = new FinancialAccount();
        newAccount.setBalance(
                financialAccountDTO.getBalance() != null ? financialAccountDTO.getBalance() : BigDecimal.ZERO);
        newAccount.setType(financialAccountDTO.getAccountType());
        newAccount.setName(financialAccountDTO.getName());
        newAccount.setUser(user);

        // Salva l'account e forza il flush per assicurarsi che venga scritto nel DB
        FinancialAccount savedAccount = financialAccountRepository.save(newAccount);
        financialAccountRepository.flush();
        return savedAccount;
    }

    public Set<Transaction> getAllTransactionsByAccountId(Long accountId) {
        FinancialAccount account = financialAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Financial account not found with id: " + accountId));
        return account.getTransactions();
    }

    public FinancialAccount getFinancialAccountById(Long accountId) {
        return financialAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Financial account not found with id: " + accountId));
    }

    @Transactional
    public void deleteAccountForUser(Long accountId, String username) {
        FinancialAccount account = getFinancialAccountByUsername(accountId, username);
        financialAccountRepository.delete(account);
    }

    /**
     * Verifica se un account appartiene a un utente specifico (per Spring Security)
     */
    public boolean isAccountOwnedByUser(Long accountId, String username) {
        try {
            FinancialAccount account = findById(accountId).orElse(null);
            if (account == null) {
                return false;
            }
            User user = userService.getUserByUsername(username);
            return account.getUser().equals(user);
        } catch (Exception e) {
            return false;
        }
    }

    /* Eseguito ogni giorno a mezzanotte */
    @Transactional
    public void postTransactionAndUpdateBalance(Transaction tx) {
        FinancialAccount account = tx.getFinancialAccount();
        BigDecimal newBalance = account.getBalance().add(tx.getAmount());
        account.setBalance(newBalance);

        account.getTransactions().add(tx);
        financialAccountRepository.save(account);
    }

}
