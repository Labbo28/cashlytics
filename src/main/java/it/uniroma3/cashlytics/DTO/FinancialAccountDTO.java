package it.uniroma3.cashlytics.DTO;

import java.math.BigDecimal;
import it.uniroma3.cashlytics.Model.Enums.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FinancialAccountDTO {

    private String name;
    @NotNull(message = "Account type is required")
    private AccountType accountType;
    @NotNull(message = "Initial balance is required")
    @DecimalMin(value = "0.0", message = "Balance cannot be negative")
    private BigDecimal balance = BigDecimal.ZERO;

}
