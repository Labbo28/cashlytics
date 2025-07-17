package it.uniroma3.cashlytics.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryDTO {

    private Long id;

    @NotBlank(message = "Il nome della categoria è obbligatorio")
    @Size(min = 2, max = 50, message = "Il nome deve essere tra 2 e 50 caratteri")
    private String name;

    @Pattern(regexp = "^https?://.*", message = "L'icona deve essere un URL valido")
    private String icon; // URL dell'icona

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Il colore deve essere in formato hex (es. #FF5733)")
    private String color; // Colore in formato hex

    private Long parentCategoryId; // Per gestire le sottocategorie
    private String parentCategoryName; // Nome della categoria padre (per display)
}
