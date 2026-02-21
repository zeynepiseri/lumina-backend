package com.lumina.backend.modules.finance.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class InvoiceResponse {
    private String invoiceNumber;
    private String patientName;
    private BigDecimal amount;
    private BigDecimal insuranceCovered;
    private BigDecimal finalAmount;
    private LocalDateTime issuedAt;
}