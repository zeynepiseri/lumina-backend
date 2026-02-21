package com.lumina.backend.modules.medicalrecord.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class PrescriptionResponse {
    private Long id;
    private String medicationName;
    private String dosage;
    private String administrationTimes;
    private String frequencyDays;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationInDays;
    private String instructions;
    private String doctorName;
    private Long doctorId;
}