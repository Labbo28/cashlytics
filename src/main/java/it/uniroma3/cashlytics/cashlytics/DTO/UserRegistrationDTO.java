package it.uniroma3.cashlytics.cashlytics.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegistrationDTO {
    
    // User credentials
    @NotBlank(message = "{NotBlank.userRegistrationDTO.username}")
    @Size(min = 3, max = 20, message = "{Size.userRegistrationDTO.username}")
    private String username;

    @NotBlank(message = "{NotBlank.userRegistrationDTO.password}")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$",
        message = "{Pattern.userRegistrationDTO.password}"
    )
    @Size(min = 8, message = "{Size.userRegistrationDTO.password}")
    private String password;

    @NotBlank(message = "{NotBlank.userRegistrationDTO.confirmPassword}")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-zA-Z]).{8,}$",
        message = "{Pattern.userRegistrationDTO.confirmPassword}"
    )
    @Size(min = 8, message = "{Size.userRegistrationDTO.confirmPassword}")
    private String confirmPassword;


    // User data
    @NotBlank(message = "{NotBlank.userRegistrationDTO.email}")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "{Pattern.userRegistrationDTO.email}"
    )
    private String email;

    @NotBlank(message = "{NotBlank.userRegistrationDTO.firstName}")
    @Size(min = 2, max = 20, message = "{Size.userRegistrationDTO.firstName}")
    private String firstName;

    @NotBlank(message = "{NotBlank.userRegistrationDTO.lastName}")
    @Size(min = 2, max = 20, message = "{Size.userRegistrationDTO.lastName}")
    private String lastName;

    @NotBlank(message = "{NotBlank.userRegistrationDTO.phoneNumber}")
    @Pattern(
        regexp = "^\\+?[0-9]{10,15}$",
        message = "{Pattern.userRegistrationDTO.phoneNumber}"
    )
    private String phoneNumber;

}
