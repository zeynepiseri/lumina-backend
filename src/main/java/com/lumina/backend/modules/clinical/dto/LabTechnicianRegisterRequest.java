package com.lumina.backend.modules.clinical.dto;

import lombok.Data;

@Data
public class LabTechnicianRegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String nationalId;
    private String phoneNumber;
    private String department; // Radiology, Biochemistry etc.
    private String employeeId; // Sicil No
}