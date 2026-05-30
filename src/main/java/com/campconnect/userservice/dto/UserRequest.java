package com.campconnect.userservice.dto;

import com.campconnect.userservice.entity.User.UserRole;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Mot de passe minimum 6 caractères")
    private String password;

    @Pattern(regexp = "^[0-9]{8}$", message = "Numéro invalide")
    private String phone;

    private UserRole role;
}