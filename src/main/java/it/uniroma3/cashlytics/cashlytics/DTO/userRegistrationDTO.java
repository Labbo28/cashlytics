package it.uniroma3.cashlytics.cashlytics.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class userRegistrationDTO {
    
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 20)
    private String username;
    @NotBlank(message = "Password cannot be blank")
    @Pattern( regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$",
    message = "Password must contain at least one letter, one number and be at least 8 characters long")
    @Size(min = 8,message = "Password must be at least 8 characters long")
    private String password;

}
