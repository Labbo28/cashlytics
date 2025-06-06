package it.uniroma3.cashlytics.cashlytics.service;

import org.springframework.stereotype.Service;

import it.uniroma3.cashlytics.cashlytics.DTO.FinancialAccountDTO;
import it.uniroma3.cashlytics.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.cashlytics.Model.User;
import it.uniroma3.cashlytics.cashlytics.Repository.FinancialAccountRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import it.uniroma3.cashlytics.cashlytics.Model.Transaction;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class FinancialAccountService {
    @Autowired
    FinancialAccountRepository financialAccountRepository;
    
    public List<FinancialAccount> getAllFinancialAccountByUsername(String username) {
        //
           return financialAccountRepository.findByUser_Credentials_Username(username);
    }

    public FinancialAccount createFinancialAccount(FinancialAccountDTO financialAccountDTO,User user) {
        FinancialAccount newAccount = new FinancialAccount();
            newAccount.setAccountType(financialAccountDTO.getAccountType());
            newAccount.setBalance(financialAccountDTO.getBalance() != null ? 
                financialAccountDTO.getBalance() : BigDecimal.ZERO);
            newAccount.setUser(user);
            
            // Salva l'account e forza il flush per assicurarsi che venga scritto nel DB
            FinancialAccount savedAccount = financialAccountRepository.save(newAccount);
            financialAccountRepository.flush();
            return savedAccount;
    }

    public  Set<Transaction> getAllTransactionsByAccountId(Long accountId) {
        FinancialAccount account = financialAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Financial account not found with id: " + accountId));
        return account.getTransactions();
       
    }

    public FinancialAccount getFinancialAccountById(Long accountId) {
        
        return financialAccountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Financial account not found with id: " + accountId));
    }
}
