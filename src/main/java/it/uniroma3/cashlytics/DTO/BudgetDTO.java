package it.uniroma3.cashlytics.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import it.uniroma3.cashlytics.Model.Enums.TransactionType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BudgetDTO {

    @NotNull
    private BigDecimal amount;
    private String description;
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    @Enumerated(EnumType.STRING)
    private RecurrencePattern recurrencePattern;
    /*
     * private Long categoryId;
     * private String categoryName;
     * private Long merchantId;
     * private String merchantName;
     */
}
