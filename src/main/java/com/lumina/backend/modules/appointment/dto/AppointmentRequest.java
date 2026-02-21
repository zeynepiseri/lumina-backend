package com.lumina.backend.modules.appointment.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentRequest {
    private Long doctorId;           // Which doctor?
    private LocalDateTime appointmentTime; // When?
    private String healthIssue;
    private String appointmentType;
    private String consultationMethod;
}