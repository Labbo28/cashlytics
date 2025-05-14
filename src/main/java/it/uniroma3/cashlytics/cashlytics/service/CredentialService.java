package it.uniroma3.cashlytics.cashlytics.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import it.uniroma3.cashlytics.cashlytics.DTO.userRegistrationDTO;
import it.uniroma3.cashlytics.cashlytics.Exceptions.userAlreadyExistsException;
import it.uniroma3.cashlytics.cashlytics.Model.Credentials;
import it.uniroma3.cashlytics.cashlytics.Repository.CredentialsRepository;

@Service
public class CredentialService {

    @Autowired
    PasswordEncoder passwordEncoder;    

    @Autowired
    private CredentialsRepository credentialsRepository;

    public void saveCredentials(userRegistrationDTO credentialsDTO) throws Exception {

        // Check if the username already exists
        if(usernameExists(credentialsDTO.getUsername())){
            throw new userAlreadyExistsException("Username already exists");
        }
        // Hash the password before saving
        String hashedPassword = passwordEncoder.encode(credentialsDTO.getPassword());
        credentialsDTO.setPassword(hashedPassword);
        // Save the credentials to the database
        Credentials credentials = new Credentials();
        credentials.setUsername(credentialsDTO.getUsername());
        credentials.setPassword(hashedPassword);
        credentialsRepository.save(credentials);

    }

    private boolean usernameExists(String username) {
        // Check if the username already exists in the database
        return credentialsRepository.findByUsername(username).isPresent();
    }
}
