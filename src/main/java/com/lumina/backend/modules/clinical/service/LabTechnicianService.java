package com.lumina.backend.modules.clinical.service;

import com.lumina.backend.modules.clinical.dto.LabTechnicianRegisterRequest;
import com.lumina.backend.modules.clinical.entity.LabTechnician;
import com.lumina.backend.modules.auth.entity.Role;
import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.clinical.repository.LabTechnicianRepository;
import com.lumina.backend.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LabTechnicianService {

    private final UserRepository userRepository;
    private final LabTechnicianRepository labTechnicianRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerLabTechnician(LabTechnicianRegisterRequest request) {
        var user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nationalId(request.getNationalId())
                .phoneNumber(request.getPhoneNumber())
                .role(Role.LAB_TECHNICIAN)
                .build();

        var savedUser = userRepository.save(user);

        var technician = LabTechnician.builder()
                .user(savedUser)
                .department(request.getDepartment())
                .employeeId(request.getEmployeeId())
                .build();

        labTechnicianRepository.save(technician);
    }
}