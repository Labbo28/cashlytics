package it.uniroma3.cashlytics.cashlytics.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserLoginDTO {

    @NotBlank(message = "{NotBlank.userLoginDTO.username}")
    private String username;
    @NotBlank(message = "{NotBlank.userLoginDTO.password}")
    private String password;

    public UserLoginDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    
}
