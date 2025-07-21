package it.uniroma3.cashlytics.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryDTO {

	private Long id;
	@NotBlank(message = "Inserisci il nome della categoria.")
	@Size(max = 50, message = "Il nome non può essere più lungo di 50 caratteri.")
	private String name;

	// @Pattern(regexp = "^fa[srb]? fa-.+", message = "La deve essere una classe
	// Font Awesome valida")
	private String icon; // URL dell'icona
	// @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Il colore deve essere in
	// formato hex")
	private String color; // Colore in formato hex

	private Long parentCategoryId; // Per gestire le sottocategorie
	private String parentCategoryName; // Nome della categoria padre (per display)

}
