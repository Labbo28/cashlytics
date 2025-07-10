package it.uniroma3.cashlytics.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.cashlytics.DTO.FinancialAccountDTO;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.Transaction;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Repository.FinancialAccountRepository;
import jakarta.transaction.Transactional;

@Service
public class FinancialAccountService {

    @Autowired
    FinancialAccountRepository financialAccountRepository;

    public List<FinancialAccount> getAllFinancialAccountByUsername(String username) {
        return financialAccountRepository.findByUser_Credentials_Username(username);
    }

    public FinancialAccount createFinancialAccount(FinancialAccountDTO financialAccountDTO, User user) {
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

    public void deleteAccount(Long accountId) {
        FinancialAccount account = financialAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Financial account not found with id: " + accountId));
        // Si suppone cascade = CascadeType.ALL sulle transazioni
        financialAccountRepository.delete(account);
    }

    @Transactional
    public void postTransactionAndUpdateBalance(Transaction tx) {
        FinancialAccount account = tx.getFinancialAccount();
        BigDecimal newBalance = account.getBalance().add(tx.getAmount());
        account.setBalance(newBalance);

        account.getTransactions().add(tx);
        financialAccountRepository.save(account);
    }

}
