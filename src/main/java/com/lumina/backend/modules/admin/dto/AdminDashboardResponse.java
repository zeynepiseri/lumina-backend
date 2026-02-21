package com.lumina.backend.modules.admin.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminDashboardResponse {
    private long totalPatients;
    private long totalDoctors;
    private long totalAppointments;

    private double monthlyEarnings;

    private List<Integer> monthlyAppointmentsData;
    private List<String> monthLabels;
    private List<TopDoctor> topDoctors;

    private String topDoctorName;
    private String topDoctorBranch;

}