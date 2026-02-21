package com.lumina.backend.modules.medicalrecord.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class PrescriptionRequest {
    private Long patientId; // Which patient involves?
    private String medicationName;
    private String dosage;
    private String administrationTimes; // "09:00, 21:00"
    private String frequencyDays;       // "Everyday"
    private LocalDate startDate;
    private Integer durationInDays;
    private String instructions;
}