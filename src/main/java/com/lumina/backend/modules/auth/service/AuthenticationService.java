package com.lumina.backend.modules.auth.service;

import com.lumina.backend.modules.auth.dto.AuthRequest;
import com.lumina.backend.modules.auth.dto.AuthResponse;
import com.lumina.backend.modules.auth.dto.RegisterRequest;
import com.lumina.backend.infrastructure.security.JwtService;
import com.lumina.backend.modules.auth.entity.Role;
import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.clinical.repository.DoctorRepository;
import com.lumina.backend.modules.clinical.repository.NurseRepository;
import com.lumina.backend.modules.clinical.repository.LabTechnicianRepository;
import com.lumina.backend.modules.patient.entity.Patient;
import com.lumina.backend.modules.patient.repository.PatientRepository;
import com.lumina.backend.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final LabTechnicianRepository labTechnicianRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final NurseRepository nurseRepository;

    @Transactional
    public AuthResponse registerPatient(RegisterRequest request) {

        var user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nationalId(request.getNationalId())
                .phoneNumber(request.getPhoneNumber())
                .role(Role.PATIENT)
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .build();

         
        var savedUser = userRepository.save(user);

        var patient = Patient.builder()
                .user(savedUser)
                .height(request.getHeight())
                .weight(request.getWeight())
                .bloodType(request.getBloodType())
                .allergies(request.getAllergies())
                .chronicDiseases(request.getChronicDiseases())
                .medications(request.getMedications())
                .build();

        patientRepository.save(patient);

         
         
        var jwtToken = jwtService.generateToken(savedUser);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .userId(savedUser.getId())
                .role(savedUser.getRole())
                .fullName(savedUser.getFullName())
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        var jwtToken = jwtService.generateToken(user);

        return AuthResponse.builder()
                .accessToken(jwtToken)
                .userId(user.getId())
                .role(user.getRole())
                .fullName(user.getFullName())
                .build();
    }
}