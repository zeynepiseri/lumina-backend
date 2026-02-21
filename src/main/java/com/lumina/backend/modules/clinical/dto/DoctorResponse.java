package com.lumina.backend.modules.clinical.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DoctorResponse {
    private Long id;
    private String fullName;
    private String title;
    private String specialty;
    private String imageUrl;
    private String branchName;
    private Long branchId;
    private String biography;
     private Double rating;
    private Integer reviewCount;
    private Integer patientCount;
    private Integer experience;
    private String diplomaNo;
    private Double consultationFee;
    private String email;
    private String phoneNumber;
    private String nationalId;
    private String gender;
    private String birthDate;
    private List<String> subSpecialties;
    private List<String> professionalExperiences;
    private List<String> educations;
     private List<String> certificates;
    private List<String> languages;
    private List<String> acceptedInsurances;
    private List<ScheduleResponse> schedules;
}