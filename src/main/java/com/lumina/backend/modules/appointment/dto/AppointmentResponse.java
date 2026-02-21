package com.lumina.backend.modules.appointment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentResponse {
    private Long id;
    private LocalDateTime appointmentTime;
    private Boolean isAvailable;
    private String status;

    private Long doctorId;
    private String doctorName;
    private String doctorTitle;
    private String doctorSpecialty;
    private String doctorImageUrl;

    private Long patientId;
    private String patientName;

    private String healthIssue;
    private String appointmentType;
    private String consultationMethod;
}