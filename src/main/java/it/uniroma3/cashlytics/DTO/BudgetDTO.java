package it.uniroma3.cashlytics.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import it.uniroma3.cashlytics.Model.Enums.TransactionType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class BudgetDTO {

    private BigDecimal amount;
    private String description;
    private LocalDateTime date;
    private boolean isRecurring;
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private Long categoryId;
    private String categoryName;
    private Long merchantId;
    private String merchantName;

}
