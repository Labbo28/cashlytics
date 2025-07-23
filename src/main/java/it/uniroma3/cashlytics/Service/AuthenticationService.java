package it.uniroma3.cashlytics.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import it.uniroma3.cashlytics.DTO.UserRegistrationDTO;
import it.uniroma3.cashlytics.Exceptions.EmailAlreadyExistsException;
import it.uniroma3.cashlytics.Exceptions.UserAlreadyExistsException;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Repository.UserRepository;

@Service
public class AuthenticationService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    public void registerUser(UserRegistrationDTO userRegistrationDTO)
            throws UserAlreadyExistsException, EmailAlreadyExistsException {
        
        // Check if the email already exists
        if (emailExists(userRegistrationDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Email already exists");
        }

        // Check if the username already exists (opzionale, per evitare duplicati URL)
        if (usernameExists(userRegistrationDTO.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists");
        }

        // Hash the password before saving
        String hashedPassword = passwordEncoder.encode(userRegistrationDTO.getPassword());

        // Create a new User object
        User user = new User(
                userRegistrationDTO.getEmail(),
                userRegistrationDTO.getUsername(),
                hashedPassword,
                userRegistrationDTO.getFirstName(),
                userRegistrationDTO.getLastName(),
                userRegistrationDTO.getPhoneNumber()
        );

        // Save the user to the database
        userRepository.save(user);
        System.out.println("Registered new local user: " + user.getEmail());
    }

    private boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    private boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}