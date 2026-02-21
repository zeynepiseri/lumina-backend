package com.lumina.backend.modules.auth.service;

import com.lumina.backend.infrastructure.security.JwtService;
import com.lumina.backend.modules.auth.dto.AuthRequest;
import com.lumina.backend.modules.auth.dto.AuthResponse;
import com.lumina.backend.modules.auth.dto.RegisterRequest;
import com.lumina.backend.modules.auth.entity.Role;
import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.auth.repository.UserRepository;
import com.lumina.backend.modules.clinical.repository.DoctorRepository;
import com.lumina.backend.modules.clinical.repository.LabTechnicianRepository;
import com.lumina.backend.modules.clinical.repository.NurseRepository;
import com.lumina.backend.modules.patient.entity.Patient;
import com.lumina.backend.modules.patient.repository.PatientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private LabTechnicianRepository labTechnicianRepository;
    @Mock private NurseRepository nurseRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @DisplayName("registerPatient - Should save User and Patient entities and return token")
    void registerPatient_ShouldSuccess() {
         
        RegisterRequest request = RegisterRequest.builder()
                .email("test@patient.com")
                .password("password123")
                .fullName("John Doe")
                .nationalId("12345678901")
                .phoneNumber("5551234567")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .email(request.getEmail())
                .role(Role.PATIENT)
                .fullName(request.getFullName())
                .build();

        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPass");
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(jwtService.generateToken(savedUser)).willReturn("jwt-token");

         
        AuthResponse response = authenticationService.registerPatient(request);

         
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getRole()).isEqualTo(Role.PATIENT);

        verify(userRepository).save(any(User.class));
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    @DisplayName("authenticate - Should authenticate user and return token")
    void authenticate_ShouldSuccess() {
         
        AuthRequest request = new AuthRequest("test@test.com", "password");
        User user = User.builder()
                .id(1L)
                .email(request.getEmail())
                .role(Role.ADMIN)
                .fullName("Admin User")
                .build();

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(jwtService.generateToken(user)).willReturn("jwt-token");

         
        AuthResponse response = authenticationService.authenticate(request);

         
        assertThat(response.getAccessToken()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}