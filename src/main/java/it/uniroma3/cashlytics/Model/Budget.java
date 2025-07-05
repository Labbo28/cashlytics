package it.uniroma3.cashlytics.Model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Budget {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;
    private String desc;
    private BigDecimal amount;
    private LocalDateTime startDate;
    private RecurrencePattern recurrencePattern;

    @ManyToOne
    @JoinColumn(name = "financial_account_id")
    private FinancialAccount financialAccount;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

}
