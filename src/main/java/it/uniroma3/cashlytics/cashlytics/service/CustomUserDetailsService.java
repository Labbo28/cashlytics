package it.uniroma3.cashlytics.cashlytics.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import it.uniroma3.cashlytics.cashlytics.Repository.CredentialsRepository;
import it.uniroma3.cashlytics.cashlytics.Model.Credentials; // Ensure this is the correct package for Credentials

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final CredentialsRepository credRepo;
    
    @Autowired
    public CustomUserDetailsService(CredentialsRepository credRepo) {
        this.credRepo = credRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Credentials cred = credRepo.findByUsername(username)
            .orElseThrow(() -> 
                new UsernameNotFoundException("Utente non trovato: " + username));

        return org.springframework.security.core.userdetails.User
            .withUsername(cred.getUsername())
            .password(cred.getPassword())
            .roles("USER")
            .build();
    }
}
