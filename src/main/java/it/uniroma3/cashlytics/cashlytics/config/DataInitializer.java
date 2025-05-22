package it.uniroma3.cashlytics.cashlytics.config;

import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import it.uniroma3.cashlytics.cashlytics.Model.User;
import it.uniroma3.cashlytics.cashlytics.Model.Credentials;
import it.uniroma3.cashlytics.cashlytics.Repository.CredentialsRepository;
import it.uniroma3.cashlytics.cashlytics.Repository.UserRepository;
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private CredentialsRepository credentialsRepository;

    @Override
    public void run(String... args) {
        if (userRepository.findByCredentials_Username("admin").isEmpty()) {
            User admin = new User("admin@cashlytics.com", "Admin", "Admin", "1234567890");
            Credentials credentials = new Credentials("admin", passwordEncoder.encode("Password123"));
            credentials.setUser(admin);
            admin.setCredentials(credentials);
            userRepository.save(admin);
            credentialsRepository.save(credentials);
        }

        User user = new User("user@cashlytics.com", "User", "User", "1234567891");
        Credentials credentials = new Credentials("user", passwordEncoder.encode("Password123"));
        credentials.setUser(user);
        user.setCredentials(credentials);
        userRepository.save(user);
        credentialsRepository.save(credentials);
    }
}