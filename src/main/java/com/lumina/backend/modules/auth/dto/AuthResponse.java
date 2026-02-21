package com.lumina.backend.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.lumina.backend.modules.auth.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("role")
    private Role role; // PATIENT, DOCTOR, ADMIN

    @JsonProperty("full_name")
    private String fullName;
}