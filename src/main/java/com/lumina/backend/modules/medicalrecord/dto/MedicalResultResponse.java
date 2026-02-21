package com.lumina.backend.modules.medicalrecord.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class MedicalResultResponse {
    private Long id;
    private String resultType;
    private String description;
    private String fileUrl;
    private LocalDateTime uploadedAt;
    private String technicianName;
}