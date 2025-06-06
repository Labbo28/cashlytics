package it.uniroma3.cashlytics.cashlytics.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import it.uniroma3.cashlytics.cashlytics.Model.Enums.TransactionType;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class TransactionDTO {

    private BigDecimal amount;
    private String description;
    private LocalDateTime date;
    private boolean isRecurring;
     @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    private Long categoryId;
    private String categoryName;   // usabile se l’utente digita un nome nuovo
    private Long merchantId;
    private String merchantName;
    

}
