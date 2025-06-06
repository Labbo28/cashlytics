package it.uniroma3.cashlytics.cashlytics.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import it.uniroma3.cashlytics.cashlytics.Model.Enums.TransactionType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Data
public class Transaction {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private BigDecimal amount;
    private String description;
    private LocalDateTime date;
    private boolean isRecurring;
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    @EqualsAndHashCode.Exclude  
    @ManyToOne
    @JoinColumn(name = "financial_account_id")
    private FinancialAccount financialAccount;
    @EqualsAndHashCode.Exclude  
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    @EqualsAndHashCode.Exclude  
    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

}
