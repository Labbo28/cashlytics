package it.uniroma3.cashlytics.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@Table(name = "cashlytics_user")
public class User {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    
    // Identificatore principale - sempre email
    private String email;
    private String username;      // Opzionale, per display/URL
    private String password;      // Nullable per utenti OAuth2
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDateTime createdAt;

    
    // Campi OAuth2
    private String provider = "LOCAL";  // "LOCAL", "GOOGLE", "GITHUB", etc.
    private String providerId;          // OAuth2 provider user ID
    private String imageUrl;            // Profile image URL da OAuth2
    
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FinancialAccount> financialAccounts = new java.util.HashSet<>();

    // Costruttore per registrazione locale
    public User(String email, String username, String password, String firstName, String lastName, String phoneNumber) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.createdAt = LocalDateTime.now();
        this.provider = "LOCAL";
     
    }
    
    // Costruttore per OAuth2
    public User(String email, String firstName, String lastName, String provider, String providerId, String imageUrl, String phoneNumber) {
        this.email = email;
        this.username = generateUsernameFromEmail(email);
        this.firstName = firstName;
        this.lastName = lastName;
        this.provider = provider;
        this.providerId = providerId;
        this.imageUrl = imageUrl;
        this.createdAt = LocalDateTime.now();
        this.phoneNumber = phoneNumber;
        
    }
    
    private String generateUsernameFromEmail(String email) {
        return email.substring(0, email.indexOf('@'));
    }

    public BigDecimal getTotalBalance() {
        return financialAccounts.stream()
                .map(FinancialAccount::getBalance)
                .filter(balance -> balance != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public boolean isOAuth2User() {
        return !"LOCAL".equals(provider);
    }
    
    @Override
    public String toString() {
        return "User{" +
               "id=" + id +
               ", email='" + email + '\'' +
               ", username='" + username + '\'' +
               ", provider='" + provider + '\'' +
               '}';
    }
}