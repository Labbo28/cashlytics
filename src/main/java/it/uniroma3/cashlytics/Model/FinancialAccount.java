package it.uniroma3.cashlytics.Model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import it.uniroma3.cashlytics.Model.Enums.AccountType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
public class FinancialAccount {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private String name;
    private AccountType type;
    // private String institution;
    private BigDecimal balance;

    @ManyToOne
    private User user;
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "financialAccount", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Transaction> transactions = new HashSet<>();
    
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "financialAccount", cascade = CascadeType.ALL)
    private Set<Budget> budgets = new HashSet<>();

    @Override
public String toString() {
    return "FinancialAccount{" +
           "id=" + id +
           ", name='" + name + '\'' +
           ", balance=" + balance +
           ", userId=" + (user != null ? user.getId() : null) +
           // Don't include transactions collection or full user object
           '}';
}

}
