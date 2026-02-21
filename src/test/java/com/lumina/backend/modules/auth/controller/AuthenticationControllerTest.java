package com.lumina.backend.modules.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.backend.infrastructure.security.JwtService;
import com.lumina.backend.modules.auth.dto.AuthRequest;
import com.lumina.backend.modules.auth.dto.AuthResponse;
import com.lumina.backend.modules.auth.dto.RegisterRequest;
import com.lumina.backend.modules.auth.service.AuthenticationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)  
class AuthenticationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private AuthenticationService authenticationService;
    @MockitoBean private JwtService jwtService;  

    @Test
    @DisplayName("POST /auth/register - Should return 200 and AuthResponse")
    void register_ShouldReturnOk() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("new@patient.com")
                .password("password123")
                .fullName("New Patient")
                .nationalId("11111111111")
                .phoneNumber("5551112233")
                .build();

        AuthResponse response = AuthResponse.builder()
                .accessToken("token")
                .userId(1L)
                .build();

        given(authenticationService.registerPatient(any(RegisterRequest.class))).willReturn(response);

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("token"));
    }

    @Test
    @DisplayName("POST /auth/register - Should return 400 when input is invalid")
    void register_ShouldReturnBadRequest_WhenInputInvalid() throws Exception {
         
        RegisterRequest request = new RegisterRequest();

        mockMvc.perform(post("/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /auth/authenticate - Should return 200 and AuthResponse")
    void authenticate_ShouldReturnOk() throws Exception {
        AuthRequest request = new AuthRequest("user@lumina.com", "password");
        AuthResponse response = AuthResponse.builder().accessToken("token").build();

        given(authenticationService.authenticate(any(AuthRequest.class))).willReturn(response);

        mockMvc.perform(post("/auth/authenticate")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("token"));
    }
}