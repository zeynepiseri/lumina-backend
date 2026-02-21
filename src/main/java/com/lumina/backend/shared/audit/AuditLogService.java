package com.lumina.backend.shared.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public void log(String action, String details) {
        String actor = "SYSTEM";
        try {
            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                actor = SecurityContextHolder.getContext().getAuthentication().getName();
            }
        } catch (Exception e) {
         }

        AuditLog log = AuditLog.builder()
                .actorEmail(actor)
                .action(action)
                .details(details)
                .build();

        auditLogRepository.save(log);
    }

    public List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }
}