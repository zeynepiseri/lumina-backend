package com.lumina.backend.modules.clinical.service;

import com.lumina.backend.modules.clinical.dto.NurseRegisterRequest;
import com.lumina.backend.modules.clinical.entity.Nurse;
import com.lumina.backend.modules.auth.entity.Role;
import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.clinical.repository.NurseRepository;
import com.lumina.backend.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NurseService {

    private final UserRepository userRepository;
    private final NurseRepository nurseRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerNurse(NurseRegisterRequest request) {
         var user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nationalId(request.getNationalId())
                .phoneNumber(request.getPhoneNumber())
                .role(Role.NURSE)
                .build();

        var savedUser = userRepository.save(user);
         var nurse = Nurse.builder()
                .user(savedUser)
                .department(request.getDepartment())
                .shiftType(request.getShiftType())
                .employeeId(request.getEmployeeId())
                .build();

        nurseRepository.save(nurse);
    }
}