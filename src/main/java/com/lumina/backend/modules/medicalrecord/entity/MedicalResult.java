package com.lumina.backend.modules.medicalrecord.entity;

import com.lumina.backend.modules.patient.entity.Patient;
import com.lumina.backend.modules.clinical.entity.LabTechnician;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "medical_results")
public class MedicalResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String resultType;  // MRI, Blood Analysis
    private String description;
    private String fileUrl;
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technician_id")
    private LabTechnician technician;
}