package it.uniroma3.cashlytics.cashlytics.Model;
import it.uniroma3.cashlytics.cashlytics.Model.Enums.AccountType;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Data;
import java.math.BigDecimal;
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
    
    @OneToMany(mappedBy = "FinancialAccount")
    private Set<Transaction> transactions;
}
