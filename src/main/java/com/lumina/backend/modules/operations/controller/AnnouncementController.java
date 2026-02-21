package com.lumina.backend.modules.operations.controller;

import com.lumina.backend.modules.operations.entity.Announcement;
import com.lumina.backend.modules.auth.entity.Role;
import com.lumina.backend.modules.operations.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public ResponseEntity<List<Announcement>> getMyAnnouncements() {
        return ResponseEntity.ok(announcementService.getMyAnnouncements());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Announcement> createAnnouncement(@RequestBody Map<String, String> request) {
        String title = request.get("title");
        String content = request.get("content");
        String roleStr = request.get("targetRole"); // "DOCTOR", "PATIENT" or null

        Role targetRole = (roleStr != null && !roleStr.isEmpty()) ? Role.valueOf(roleStr) : null;

        return ResponseEntity.ok(announcementService.createAnnouncement(title, content, targetRole));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.ok().build();
    }
}