package it.uniroma3.cashlytics.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import it.uniroma3.cashlytics.Model.CustomOAuth2User;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Model.UserPrincipal;
import it.uniroma3.cashlytics.Repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    /**
     * Ottiene l'utente correntemente autenticato (sia OAuth2 che form login)
     */
    public User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal instanceof UserPrincipal) {
            return ((UserPrincipal) principal).getUser();
        } else if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getUser();
        }
        
        // Fallback: prova a recuperare per email
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return getUserByEmail(email);
    }



    /**
     * Verifica se l'utente corrente è un utente OAuth2
     */
    public boolean isCurrentUserOAuth2() {
        try {
            User currentUser = getCurrentUser();
            return currentUser.isOAuth2User();
        } catch (Exception e) {
            return false;
        }
    }
}