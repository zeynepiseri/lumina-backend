package com.lumina.backend.modules.finance.controller;

import com.lumina.backend.modules.finance.dto.InvoiceRequest;
import com.lumina.backend.modules.finance.dto.InvoiceResponse;
import com.lumina.backend.modules.finance.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final FinanceService financeService;
     @PostMapping
    @PreAuthorize("hasAnyRole('REGISTRAR', 'ADMIN')")
    public ResponseEntity<InvoiceResponse> createInvoice(@RequestBody InvoiceRequest request) {
        return ResponseEntity.ok(financeService.createInvoice(request));
    }
     @GetMapping("/reports/daily-collection")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, BigDecimal>> getDailyReport() {
        BigDecimal total = financeService.getDailyCollection();
        return ResponseEntity.ok(Map.of("totalDailyCollection", total != null ? total : BigDecimal.ZERO));
    }
}