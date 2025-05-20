package it.uniroma3.cashlytics.cashlytics.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import it.uniroma3.cashlytics.cashlytics.DTO.userRegistrationDTO;
import it.uniroma3.cashlytics.cashlytics.Exceptions.EmailAlreadyExistsException;
import it.uniroma3.cashlytics.cashlytics.Exceptions.UserAlreadyExistsException;
import it.uniroma3.cashlytics.cashlytics.Model.Credentials;
import it.uniroma3.cashlytics.cashlytics.Model.User;
import it.uniroma3.cashlytics.cashlytics.Repository.CredentialsRepository;
import it.uniroma3.cashlytics.cashlytics.Repository.UserRepository;

@Service
public class AutenticationService {

    @Autowired
    PasswordEncoder passwordEncoder;    

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CredentialsRepository credentialsRepository;

    public void registerUser(userRegistrationDTO userRegistrationDTO) throws UserAlreadyExistsException, EmailAlreadyExistsException {

        // Check if the username already exists
        if(usernameExists(userRegistrationDTO.getUsername())){
            throw new UserAlreadyExistsException("Username already exists");
        }
        // Check if the email already exists
        if(emailExists(userRegistrationDTO.getEmail())){
            throw new EmailAlreadyExistsException("Email already exists");
        }
        // Hash the password before saving
        String hashedPassword = passwordEncoder.encode(userRegistrationDTO.getPassword());
        
       
        // Create a new Credentials object
        Credentials credentials = new Credentials(userRegistrationDTO.getUsername(), hashedPassword);
        
        // Create a new User object
        User user = new User(userRegistrationDTO.getEmail(),
        userRegistrationDTO.getFirstName(),
        userRegistrationDTO.getLastName(),
        userRegistrationDTO.getPhoneNumber());

       //create link between user and credentials
        user.setCredentials(credentials);
        credentials.setUser(user);

        // Save the user and credentials to the database
        credentialsRepository.save(credentials);
        userRepository.save(user);


    }

    private boolean usernameExists(String username) {
        // Check if the username already exists in the database
        return credentialsRepository.findByUsername(username).isPresent();
    }

    private boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
