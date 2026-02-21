package com.lumina.backend.modules.clinical.dto;

import lombok.Data;

@Data
public class ScheduleRequest {
    private String dayOfWeek;
    private String startTime;
    private String endTime;
}