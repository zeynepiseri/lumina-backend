package com.lumina.backend.modules.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopDoctor {
    private String name;
    private String branch;
    private long appointmentCount;
    private String imageUrl;
    private double rating;
}