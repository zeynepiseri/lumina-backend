package com.lumina.backend.modules.clinical.controller;

import com.lumina.backend.modules.clinical.dto.LabTechnicianRegisterRequest;
import com.lumina.backend.modules.clinical.dto.NurseRegisterRequest;
import com.lumina.backend.modules.auth.dto.UserResponse;
import com.lumina.backend.modules.auth.entity.Role;
import com.lumina.backend.modules.auth.repository.UserRepository;
import com.lumina.backend.modules.clinical.service.LabTechnicianService;
import com.lumina.backend.modules.clinical.service.NurseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
 @PreAuthorize("hasRole('ADMIN')")
public class StaffController {

    private final NurseService nurseService;
    private final LabTechnicianService labTechnicianService;
    private final UserRepository userRepository;

    @PostMapping("/register-nurse")
    public ResponseEntity<String> registerNurse(@RequestBody NurseRegisterRequest request) {
        nurseService.registerNurse(request);
        return ResponseEntity.ok("Nurse registered successfully.");
    }

    @PostMapping("/register-technician")
    public ResponseEntity<String> registerLabTechnician(@RequestBody LabTechnicianRegisterRequest request) {
        labTechnicianService.registerLabTechnician(request);
        return ResponseEntity.ok("Lab Technician registered successfully.");
    }
     @GetMapping("/by-role/{roleName}")
    public ResponseEntity<List<UserResponse>> getStaffByRole(@PathVariable String roleName) {
        try {
            Role role = Role.valueOf(roleName.toUpperCase());
             if(role == Role.PATIENT) throw new IllegalArgumentException("Cannot list patients via staff endpoint");

            List<UserResponse> staffList = userRepository.findByRole(role)
                    .stream()
                    .map(user -> UserResponse.builder()
                            .id(user.getId())
                            .fullName(user.getFullName())
                            .email(user.getEmail())
                            .phoneNumber(user.getPhoneNumber())
                            .nationalId(user.getNationalId())
                            .imageUrl(user.getImageUrl())
                            .role(user.getRole())
                            .build())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(staffList);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid Role or Operation.");
        }
    }
}