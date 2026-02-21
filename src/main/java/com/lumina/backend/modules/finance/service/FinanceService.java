package com.lumina.backend.modules.finance.service;

import com.lumina.backend.modules.finance.dto.InvoiceRequest;
import com.lumina.backend.modules.finance.dto.InvoiceResponse;
import com.lumina.backend.modules.appointment.entity.Appointment;
import com.lumina.backend.modules.finance.entity.Invoice;
import com.lumina.backend.shared.exception.ResourceNotFoundException;
import com.lumina.backend.modules.appointment.repository.AppointmentRepository;
import com.lumina.backend.modules.finance.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FinanceService {

    private final InvoiceRepository invoiceRepository;
    private final AppointmentRepository appointmentRepository;

    // Muayene Ücretleri (Sabit veya DB'den çekilebilir)
    private static final BigDecimal BASE_FEE = BigDecimal.valueOf(1000.00);

    public InvoiceResponse createInvoice(InvoiceRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        // Zaten fatura kesilmiş mi?
        // (Bunun için Appointment entity'sine OneToOne ilişkiyi çift taraflı yapmak veya repo sorgusu gerekir)

        BigDecimal totalAmount = BASE_FEE;
        BigDecimal insuranceCoverage = BigDecimal.ZERO;

        // Sigorta Mantığı
        if (request.getInsuranceCompany() != null && !request.getInsuranceCompany().isEmpty()) {
            // Örn: SGK %40 karşılıyor, Özel %80
            if (request.getInsuranceCompany().equalsIgnoreCase("SGK")) {
                insuranceCoverage = totalAmount.multiply(BigDecimal.valueOf(0.40));
            } else {
                insuranceCoverage = totalAmount.multiply(BigDecimal.valueOf(0.80));
            }
        }

        BigDecimal patientToPay = totalAmount.subtract(insuranceCoverage);

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-" + UUID.randomUUID().toString().substring(0,8).toUpperCase())
                .appointment(appointment)
                .patient(appointment.getPatient())
                .amount(totalAmount)
                .insuranceCovered(insuranceCoverage)
                .patientPaid(patientToPay)
                .insuranceCompany(request.getInsuranceCompany())
                .paymentMethod(request.getPaymentMethod())
                .isPaid(true) // Vezne işlemi olduğu için ödendi sayıyoruz
                .issuedAt(LocalDateTime.now())
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        return mapToResponse(saved);
    }

    // Tahsilat Raporu
    public BigDecimal getDailyCollection() {
        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0);
        LocalDateTime end = LocalDateTime.now().withHour(23).withMinute(59);
        return invoiceRepository.sumTotalCollection(start, end);
    }

    private InvoiceResponse mapToResponse(Invoice invoice) {
        // ... DTO mapping (Builder pattern)
        return InvoiceResponse.builder()
                .invoiceNumber(invoice.getInvoiceNumber())
                .patientName(invoice.getPatient().getFullName())
                .amount(invoice.getAmount())
                .insuranceCovered(invoice.getInsuranceCovered())
                .finalAmount(invoice.getPatientPaid())
                .issuedAt(invoice.getIssuedAt())
                .build();
    }
}