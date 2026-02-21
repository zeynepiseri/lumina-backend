package com.lumina.backend.modules.clinical.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class DoctorRequest {
    private String fullName;
    private String email;
    private String password;
    private String nationalId;
    private String phoneNumber;
    private String gender;
    private LocalDate birthDate;
    private String imageUrl;
    private String title;
    private String specialty;
    private String diplomaNo;
    private String biography;
    private Long branchId;
    private Integer experience;
    private String patients;
    private String reviews;
    private List<ScheduleRequest> schedules;
    private List<String> subSpecialties;
    private List<String> professionalExperiences;
    private List<String> educations;
    private List<String> acceptedInsurances;
    private List<String> certificates;
    private List<String> languages;
}