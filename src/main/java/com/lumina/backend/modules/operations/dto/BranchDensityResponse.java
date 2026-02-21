package com.lumina.backend.modules.operations.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BranchDensityResponse {
    private String branchName;
    private Long totalAppointmentsToday;
    private Long activeDoctors;
    private Double occupancyRate;
}