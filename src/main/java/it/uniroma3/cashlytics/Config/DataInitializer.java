package it.uniroma3.cashlytics.Config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import it.uniroma3.cashlytics.Model.Credentials;
import it.uniroma3.cashlytics.Model.FinancialAccount;
import it.uniroma3.cashlytics.Model.Transaction;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Model.Enums.AccountType;
import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import it.uniroma3.cashlytics.Model.Enums.TransactionType;
import it.uniroma3.cashlytics.Repository.CredentialsRepository;
import it.uniroma3.cashlytics.Repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CredentialsRepository credentialsRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        System.out.println("Running DataInitializer...");
        if (userRepository.findByCredentials_Username("admin").isEmpty()) {
            User admin = new User("admin@cashlytics.com", "Admin", "Admin", "1234567890");
            admin.setFinancialAccounts(new HashSet<>()); // Importante!

            Credentials adminCreds = new Credentials("admin", passwordEncoder.encode("Password123"));
            adminCreds.setUser(admin);
            admin.setCredentials(adminCreds);

            userRepository.save(admin);
            credentialsRepository.save(adminCreds);
        }

        User user;
        if (userRepository.findByCredentials_Username("user").isEmpty()) {
            user = new User("user@cashlytics.com", "User", "User", "1234567891");
            user.setFinancialAccounts(new HashSet<>()); // Importante!

            Credentials userCreds = new Credentials("user", passwordEncoder.encode("Password123"));
            userCreds.setUser(user);
            user.setCredentials(userCreds);

            userRepository.save(user);
            credentialsRepository.save(userCreds);
        } else {
            user = userRepository.findByCredentials_Username("user").orElseThrow();
            if (user.getFinancialAccounts() == null) {
                user.setFinancialAccounts(new HashSet<>()); // fallback
            }
        }
        if (user.getFinancialAccounts().isEmpty()) {
            initUserData(user);
        }
    }

    /*
     * private void resetAccounts(User user) {
     * user.getFinancialAccounts().clear();
     * userRepository.save(user);
     * }
     */

    private void initUserData(User user) {
        // Crea account
        FinancialAccount account = new FinancialAccount();
        account.setName("Main Account");
        account.setType(AccountType.Carta_Credito);
        account.setBalance(BigDecimal.valueOf(2000));
        account.setUser(user);
        account.setTransactions(new HashSet<>());

        // Crea transazione
        Transaction tx1 = new Transaction();
        tx1.setAmount(BigDecimal.valueOf(100));
        tx1.setDescription("Gambling");
        tx1.setDate(LocalDateTime.now());
        tx1.setTransactionType(TransactionType.INCOME);
        tx1.setRecurrence(RecurrencePattern.UNA_TANTUM);
        tx1.setRecurring(false);
        tx1.setFinancialAccount(account);

        // Crea una transazione ricorrente
        Transaction tx2 = new Transaction();
        tx2.setAmount(BigDecimal.valueOf(-50));
        tx2.setDescription("Monthly subscription");
        tx2.setDate(LocalDateTime.now());
        tx2.setTransactionType(TransactionType.EXPENSE);
        tx2.setRecurrence(RecurrencePattern.Mensile);
        tx2.setRecurring(true);
        tx2.setStartDate(LocalDateTime.now().toLocalDate());
        tx2.setLastGenerated(LocalDateTime.now().toLocalDate());
        tx2.setFinancialAccount(account);

        // Relazioni bidirezionali
        account.getTransactions().add(tx1);
        account.getTransactions().add(tx2);
        account.setBalance(account.getBalance().add(tx1.getAmount()).add(tx2.getAmount()));
        user.getFinancialAccounts().add(account);

        // Salva l’intera struttura
        userRepository.save(user);
    }
}
