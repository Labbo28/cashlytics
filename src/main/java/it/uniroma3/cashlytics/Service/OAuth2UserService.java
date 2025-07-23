package it.uniroma3.cashlytics.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import it.uniroma3.cashlytics.Model.CustomOAuth2User;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Repository.UserRepository;

@Service
public class OAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        
        return processOAuth2User(userRequest, oauth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oauth2User) {
        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        String email = oauth2User.getAttribute("email");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");
        String providerId = oauth2User.getAttribute("sub");
        String imageUrl = oauth2User.getAttribute("picture");
        
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            // Crea nuovo utente OAuth2
            user = new User(email, firstName, lastName, provider, providerId, imageUrl);
            userRepository.save(user);
            System.out.println("Created new OAuth2 user: " + email + " with provider: " + provider);
        } else if ("LOCAL".equals(user.getProvider())) {
            // Utente già esistente con auth locale
            throw new OAuth2AuthenticationException("Email già registrata con autenticazione locale");
        } else if (!provider.equals(user.getProvider())) {
            // Utente già esistente ma con provider diverso
            throw new OAuth2AuthenticationException("Email già registrata con un altro provider: " + user.getProvider());
        }
        // Se l'utente esiste già con lo stesso provider, procedi normalmente
        
        return new CustomOAuth2User(oauth2User, user);
    }
}