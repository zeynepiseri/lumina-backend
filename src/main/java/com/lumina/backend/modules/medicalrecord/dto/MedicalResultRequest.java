package com.lumina.backend.modules.medicalrecord.dto;

import lombok.Data;

@Data
public class MedicalResultRequest {
    private Long patientId;
    private String resultType; // MRI, ANALYSIS, etc.
    private String description;
    private String fileUrl; // URL from AttachmentService upload
}