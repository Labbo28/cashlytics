package it.uniroma3.cashlytics.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDateTime createdAt;

    public User(String email, String firstName, String lastName, String phoneNumber) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.createdAt = LocalDateTime.now();
    }

    @EqualsAndHashCode.Exclude
    @OneToOne(cascade = jakarta.persistence.CascadeType.ALL, fetch = jakarta.persistence.FetchType.EAGER)
    private Credentials credentials;
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "user")
    private Set<FinancialAccount> financialAccounts;

    /*
     * @EqualsAndHashCode.Exclude
     * 
     * @OneToMany(mappedBy = "user")
     * private Set<Merchant> merchants;
     * 
     * @EqualsAndHashCode.Exclude
     * 
     * @OneToMany(mappedBy = "user")
     * private Set<Category> categories;
     */

    public BigDecimal getTotalBalance() {
        return financialAccounts.stream()
                .map(FinancialAccount::getBalance)
                .filter(balance -> balance != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
