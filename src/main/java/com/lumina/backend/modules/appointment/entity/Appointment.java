package com.lumina.backend.modules.appointment.entity;

import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.clinical.entity.Doctor;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime appointmentTime;

    private Boolean isAvailable;

    private String patientName;
    private String healthIssue;
    private String appointmentType;
    private String consultationMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User patient;

    public String getDoctorName() {
        return doctor != null ? doctor.getFullName() : "Unknown Doctor";
    }

    public String getPatientNameField() {
        return patient != null ? patient.getFullName() : (patientName != null ? patientName : "Unknown Patient");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Appointment that = (Appointment) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}