package com.lumina.backend.modules.admin.controller;

import com.lumina.backend.modules.admin.dto.AdminDashboardResponse;
import com.lumina.backend.modules.operations.dto.BranchDensityResponse;
import com.lumina.backend.modules.auth.service.AuthenticationService;
import com.lumina.backend.shared.audit.AuditLog;
import com.lumina.backend.modules.auth.repository.UserRepository;
import com.lumina.backend.shared.audit.AuditLogService;
import com.lumina.backend.modules.admin.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StatisticsService statisticsService;
    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;


    @GetMapping("/dashboard-stats")
    public ResponseEntity<AdminDashboardResponse> getDashboardStats() {
        return ResponseEntity.ok(statisticsService.getDashboardStats());
    }

    @GetMapping("/polyclinic-density")
    @PreAuthorize("hasAnyRole('ADMIN', 'REGISTRAR')")
    public ResponseEntity<List<BranchDensityResponse>> getPolyclinicDensity() {
        return ResponseEntity.ok(statisticsService.getPolyclinicDensity());
    }

    @GetMapping("/audit-logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLog>> getSystemLogs() {
        return ResponseEntity.ok(auditLogService.getRecentLogs());
    }
}