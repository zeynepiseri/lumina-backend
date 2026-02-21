package com.lumina.backend.modules.patient.controller;

import com.lumina.backend.modules.patient.dto.PatientResponse;
import com.lumina.backend.modules.patient.service.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'REGISTRAR', 'NURSE')")
    public ResponseEntity<Page<PatientResponse>> getAllPatients(Pageable pageable) {
        return ResponseEntity.ok(patientService.getAllPatients(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'REGISTRAR', 'NURSE')")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientResponse> getMyProfile(Principal principal) {
        return ResponseEntity.ok(patientService.getMyProfile(principal.getName()));
    }

    @PatchMapping("/me/vitals")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Void> updateMyVitals(
            @RequestBody Map<String, Object> updates,
            Principal principal
    ) {
        Double height = updates.containsKey("height") ? Double.valueOf(updates.get("height").toString()) : null;
        Double weight = updates.containsKey("weight") ? Double.valueOf(updates.get("weight").toString()) : null;
        String bloodType = (String) updates.get("bloodType");

        patientService.updateMyVitals(principal.getName(), height, weight, bloodType);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/vitals")
    @PreAuthorize("hasRole('DOCTOR', 'NURSE')")
    public ResponseEntity<Void> updateVitals(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates
    ) {
        Double height = updates.containsKey("height") ? Double.valueOf(updates.get("height").toString()) : null;
        Double weight = updates.containsKey("weight") ? Double.valueOf(updates.get("weight").toString()) : null;
        String bloodType = (String) updates.get("bloodType");

        patientService.updatePatientVitals(id, height, weight, bloodType);
        return ResponseEntity.ok().build();
    }
}