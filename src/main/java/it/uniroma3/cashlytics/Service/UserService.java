package it.uniroma3.cashlytics.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Repository.UserRepository;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public User getUserByUsername(String username) {
        return userRepository.findByCredentials_Username(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

}
