package com.lumina.backend.modules.finance.dto;

import lombok.Data;

@Data
public class InvoiceRequest {
    private Long appointmentId;
    private String insuranceCompany; // Optional
    private String paymentMethod;    // CASH, CARD
}