package com.lumina.backend.modules.clinical.dto;

import lombok.Data;

@Data
public class NurseRegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String nationalId;
    private String phoneNumber;
    private String department;
    private String shiftType;
    private String employeeId;
}