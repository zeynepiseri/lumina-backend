package com.lumina.backend.modules.auth.dto;

import com.lumina.backend.modules.auth.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String nationalId;
    private String imageUrl;
    private String gender;
    private LocalDate birthDate;
    private Role role;
}