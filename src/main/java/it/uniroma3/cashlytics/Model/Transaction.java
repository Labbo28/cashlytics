package it.uniroma3.cashlytics.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import it.uniroma3.cashlytics.Model.Enums.TransactionType;
import jakarta.persistence.Column;
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
    @Column(name = "amount",nullable = false)
    private BigDecimal amount;

    @Column(name = "date",nullable = false)
    private LocalDateTime date;

    @Column(name = "description")
    private String description;

    @ManyToOne @EqualsAndHashCode.Exclude @JoinColumn(name = "financial_account_id")
    private FinancialAccount financialAccount;

    @Column(name = "is_recurring",nullable = false)
    private boolean isRecurring;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence", nullable = false)
    private RecurrencePattern recurrence;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type",nullable = false)
    private TransactionType transactionType;

   
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "merchant_id")
    private Merchant merchant;

}
