package com.lumina.backend.modules.finance.repository;

import com.lumina.backend.modules.finance.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByPatientId(Long patientId);
     @Query("SELECT SUM(i.patientPaid) FROM Invoice i WHERE i.isPaid = true AND i.issuedAt BETWEEN :start AND :end")
    BigDecimal sumTotalCollection(LocalDateTime start, LocalDateTime end);
     List<Invoice> findByInsuranceCompany(String insuranceCompany);
}