package com.lumina.backend.modules.medicalrecord.entity;

import com.lumina.backend.modules.patient.entity.Patient;
import com.lumina.backend.modules.clinical.entity.Doctor;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String medicationName;

    // Dosage: e.g., "1 tablet", "5ml"
    private String dosage;

    // Frequency details
    // e.g., "08:00, 20:00" or "Morning, Night"
    private String administrationTimes;

    // e.g., "Everyday", "Monday, Wednesday"
    private String frequencyDays;

    private LocalDate startDate;
    private LocalDate endDate;

    // Duration in days (calculated or manual)
    private Integer durationInDays;

    // Special instructions: "Take on a full stomach"
    @Column(columnDefinition = "TEXT")
    private String instructions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor; // The doctor who prescribed this
}