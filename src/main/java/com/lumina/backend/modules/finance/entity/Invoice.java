package com.lumina.backend.modules.finance.entity;

import com.lumina.backend.modules.appointment.entity.Appointment;
import com.lumina.backend.modules.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invoice")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String invoiceNumber;

    @OneToOne
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private User patient;
    private BigDecimal amount;
    private BigDecimal insuranceCovered;
    private BigDecimal patientPaid;
    private String insuranceCompany;
    private Boolean isPaid;
    private LocalDateTime issuedAt;
    private String paymentMethod;
}