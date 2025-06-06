package it.uniroma3.cashlytics.cashlytics.Model;
import it.uniroma3.cashlytics.cashlytics.Model.Enums.AccountType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
public class FinancialAccount {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private AccountType accountType;
    private BigDecimal balance;
    
    @ManyToOne
    private User user;
    @EqualsAndHashCode.Exclude  
    @OneToMany(mappedBy = "financialAccount", cascade = CascadeType.ALL)
    private Set<Transaction> transactions = new HashSet<>();
}