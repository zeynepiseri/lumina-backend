package com.lumina.backend.modules.clinical.controller;

import com.lumina.backend.modules.appointment.dto.AppointmentResponse;
import com.lumina.backend.modules.clinical.dto.DoctorRegisterRequest;
import com.lumina.backend.modules.clinical.dto.DoctorRequest;
import com.lumina.backend.modules.clinical.dto.DoctorResponse;
import com.lumina.backend.modules.appointment.service.AppointmentService;
import com.lumina.backend.modules.clinical.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;
    private final AppointmentService appointmentService;

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> getAllDoctors(@RequestParam(required = false) Long branchId) {
        return ResponseEntity.ok(doctorService.getAllDoctors(branchId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @GetMapping("/{id}/appointments")
    public ResponseEntity<List<AppointmentResponse>> getDoctorAppointmentsByDate(
            @PathVariable Long id,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getAppointmentsByDoctorAndDate(id, date));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponse> createDoctor(@RequestBody DoctorRegisterRequest request) {
        return ResponseEntity.ok(doctorService.createDoctor(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable Long id,
            @RequestBody DoctorRequest request,
            Authentication authentication
    ) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            String currentUserEmail = authentication.getName();
            doctorService.validateDoctorOwnership(id, currentUserEmail);
        }

        return ResponseEntity.ok(doctorService.updateDoctor(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok().build();
    }
}