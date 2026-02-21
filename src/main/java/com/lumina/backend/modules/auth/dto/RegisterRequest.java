package com.lumina.backend.modules.auth.dto;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Full name cannot be empty")
    private String fullName;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @NotBlank(message = "National ID cannot be empty")
    @Size(min = 6, max = 20, message = "National ID must be between 6 and 20 characters")
    private String nationalId;

    @NotBlank(message = "Phone number cannot be empty")
    @Size(min = 10, max = 15, message = "Phone number must be valid")
    private String phoneNumber;

    private Double height;
    private Double weight;
    private String bloodType;
    private String gender;
    private LocalDate birthDate;
    private List<String> allergies;
    private List<String> chronicDiseases;
    private List<String> medications;
}