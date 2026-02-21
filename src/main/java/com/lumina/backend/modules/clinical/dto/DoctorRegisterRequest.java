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
public class DoctorRegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String nationalId;
    private String phoneNumber;
    private String gender;       
    private String imageUrl;
    private Long branchId;
    private String title;
    private String specialty;
    private String diplomaNo;
    private String biography;    
    private Integer experience;
    private List<String> acceptedInsurances;
    private List<String> subSpecialties;
    private List<String> professionalExperiences;
    private List<String> educations;
    private List<String> certificates;
    private List<String> languages;
    private List<ScheduleRequest> schedules;
}