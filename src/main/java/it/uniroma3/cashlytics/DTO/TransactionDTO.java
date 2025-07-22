package it.uniroma3.cashlytics.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

import it.uniroma3.cashlytics.Model.Enums.RecurrencePattern;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransactionDTO implements CategorizableDTO {

    // Importo della transazione (positivo o negativo)
    @NotNull(message = "Inserisci un importo.")
    private BigDecimal amount;

    // Descrizione libera
    private String description;

    // Data della transazione
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    // Ricorrenza (può essere null per 'una tantum')
    private RecurrencePattern recurrencePattern;

    // ID del merchant selezionato (se esistente)
    private Long merchantId;

    // Nome del nuovo merchant (se inserito)
    private String merchantName;

    // ID della categoria selezionata (se esistente)
    private Long categoryId;

    // Nome della nuova categoria (se inserita)
    private String categoryName;

    // Icona e colore solo per nuova categoria
    private String categoryIcon;
    private String categoryColor;
}
