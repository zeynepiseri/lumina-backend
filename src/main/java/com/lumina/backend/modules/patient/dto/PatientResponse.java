package com.lumina.backend.modules.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatientResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String nationalId;
    private String imageUrl;
    private String gender;
    private LocalDate birthDate;
    private Double height;
    private Double weight;
    private String bloodType;
    private List<String> allergies;
    private List<String> chronicDiseases;
}