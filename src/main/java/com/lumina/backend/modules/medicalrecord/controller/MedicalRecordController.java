package com.lumina.backend.modules.medicalrecord.controller;

import com.lumina.backend.modules.medicalrecord.dto.MedicalResultRequest;
import com.lumina.backend.modules.medicalrecord.dto.MedicalResultResponse;
import com.lumina.backend.modules.medicalrecord.dto.PrescriptionRequest;
import com.lumina.backend.modules.medicalrecord.dto.PrescriptionResponse;
import com.lumina.backend.modules.medicalrecord.entity.MedicalResult;
import com.lumina.backend.modules.medicalrecord.entity.Prescription;
import com.lumina.backend.modules.medicalrecord.repository.PrescriptionRepository;
import com.lumina.backend.modules.medicalrecord.service.MedicalRecordService;
import com.lumina.backend.modules.medicalrecord.service.PrescriptionPdfService;
import com.lumina.backend.shared.exception.ResourceNotFoundException;
import com.lumina.backend.shared.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final PrescriptionPdfService prescriptionPdfService;
    private final PrescriptionRepository prescriptionRepository;
    private final EmailService emailService;

    @PostMapping("/prescriptions")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Prescription> createPrescription(@RequestBody PrescriptionRequest request) {
        return ResponseEntity.ok(medicalRecordService.addPrescription(request));
    }

    @PostMapping("/results")
    @PreAuthorize("hasRole('LAB_TECHNICIAN')")
    public ResponseEntity<MedicalResult> addMedicalResult(@RequestBody MedicalResultRequest request) {
        return ResponseEntity.ok(medicalRecordService.addMedicalResult(request));
    }

    @PatchMapping("/patient/{patientId}/vitals")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<Void> updateVitals(
            @PathVariable Long patientId,
            @RequestBody Map<String, Object> updates
    ) {
        Double height = updates.containsKey("height") ? Double.valueOf(updates.get("height").toString()) : null;
        Double weight = updates.containsKey("weight") ? Double.valueOf(updates.get("weight").toString()) : null;
        String bloodType = (String) updates.get("bloodType");

        medicalRecordService.updatePatientVitals(patientId, height, weight, bloodType);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-prescriptions")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<PrescriptionResponse>> getMyPrescriptions() {
        return ResponseEntity.ok(medicalRecordService.getMyPrescriptions());
    }

    @GetMapping("/my-results")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<MedicalResultResponse>> getMyResults() {
        return ResponseEntity.ok(medicalRecordService.getMyResults());
    }


    @GetMapping("/prescriptions/{id}/pdf")
    @PreAuthorize("hasAnyAuthority('PATIENT', 'ROLE_PATIENT', 'DOCTOR', 'ROLE_DOCTOR', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<byte[]> downloadPrescriptionPdf(@PathVariable Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = auth.getName();
        boolean isAdminOrDoctor = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_DOCTOR"));
        if (!isAdminOrDoctor) {
            String patientEmail = prescription.getPatient().getUser().getEmail();
            if (!patientEmail.equals(currentEmail)) {
                throw new AccessDeniedException("You are not authorized to view this prescription.");
            }
        }
        byte[] pdfBytes = prescriptionPdfService.generatePrescriptionPdf(prescription);
        String patientName = prescription.getPatient() != null ?
                prescription.getPatient().getFullName().replaceAll(" ", "_") : "Patient";
        String fileName = "Prescription_" + patientName + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PostMapping("/prescriptions/{id}/email")
    @PreAuthorize("hasAnyAuthority('PATIENT', 'ROLE_PATIENT', 'DOCTOR', 'ROLE_DOCTOR', 'ADMIN', 'ROLE_ADMIN')")
    public ResponseEntity<String> sendPrescriptionEmail(@PathVariable Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));
        byte[] pdfBytes = prescriptionPdfService.generatePrescriptionPdf(prescription);

        String patientName = prescription.getPatient() != null ? prescription.getPatient().getFullName() : "Valued Patient";
        String patientEmail = prescription.getPatient() != null ? prescription.getPatient().getEmail() : null;
        String fileName = "Prescription_" + id + ".pdf";

        if (patientEmail == null || patientEmail.isEmpty()) {
            return ResponseEntity.badRequest().body("Patient does not have an email address.");
        }
        emailService.sendPrescriptionEmail(patientEmail, patientName, pdfBytes, fileName);

        return ResponseEntity.ok("Email sending process started for: " + patientEmail);
    }

    @GetMapping("/prescriptions/{id}/verify")
    public ResponseEntity<Map<String, Object>> verifyPrescription(@PathVariable Long id) {
        Prescription prescription = prescriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));

        Map<String, Object> verificationData = Map.of(
                "status", "✅ VALID PRESCRIPTION",
                "prescriptionId", prescription.getId(),
                "patientName", prescription.getPatient().getFullName(),
                "medication", prescription.getMedicationName(),
                "doctor", prescription.getDoctor().getFullName(),
                "date", prescription.getStartDate().toString(),
                "verifiedAt", java.time.LocalDateTime.now().toString()
        );

        return ResponseEntity.ok(verificationData);
    }
}