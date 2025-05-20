package it.uniroma3.cashlytics.cashlytics.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import it.uniroma3.cashlytics.cashlytics.DTO.UserLoginDTO;
import it.uniroma3.cashlytics.cashlytics.DTO.UserRegistrationDTO;
import it.uniroma3.cashlytics.cashlytics.Exceptions.EmailAlreadyExistsException;
import it.uniroma3.cashlytics.cashlytics.Exceptions.UserAlreadyExistsException;
import it.uniroma3.cashlytics.cashlytics.Exceptions.UserDoesNotExistsException;
import it.uniroma3.cashlytics.cashlytics.Exceptions.WrongPasswordException;
import it.uniroma3.cashlytics.cashlytics.Model.Credentials;
import it.uniroma3.cashlytics.cashlytics.Model.User;
import it.uniroma3.cashlytics.cashlytics.Repository.CredentialsRepository;
import it.uniroma3.cashlytics.cashlytics.Repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;

@Service
public class AutenticationService {
    @Autowired
    PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CredentialsRepository credentialsRepository;

    public User loginUser(UserLoginDTO userLoginDTO) throws UserAlreadyExistsException {
        // Check if the username exists
        if(!usernameExists(userLoginDTO.getUsername())){
            throw new UserDoesNotExistsException("Username does not exist");
        }
        
        // Check if the password is correct
        Credentials credentials = credentialsRepository.findByUsername(userLoginDTO.getUsername()).orElseThrow();
        if(!passwordEncoder.matches(userLoginDTO.getPassword(), credentials.getPassword())){
            throw new WrongPasswordException("Incorrect password");
        }
        else{
            // Ottieni l'utente collegato alle credenziali
            User user = credentials.getUser();
            
            // Crea un oggetto Authentication per Spring Security
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                userLoginDTO.getUsername(),
                null, // password non necessaria qui perché abbiamo già verificato
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );
            
            // Imposta l'autenticazione nel SecurityContextHolder
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            return user;
        }
    }

    public void registerUser(UserRegistrationDTO userRegistrationDTO) throws UserAlreadyExistsException, EmailAlreadyExistsException {
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