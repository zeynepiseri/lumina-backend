package com.lumina.backend.modules.medicalrecord.service;

import com.lumina.backend.modules.medicalrecord.dto.MedicalResultRequest;
import com.lumina.backend.modules.medicalrecord.dto.MedicalResultResponse;
import com.lumina.backend.modules.medicalrecord.dto.PrescriptionRequest;
import com.lumina.backend.modules.medicalrecord.dto.PrescriptionResponse;
import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.auth.repository.UserRepository;
import com.lumina.backend.modules.clinical.entity.Doctor;
import com.lumina.backend.modules.clinical.entity.LabTechnician;
import com.lumina.backend.modules.clinical.repository.DoctorRepository;
import com.lumina.backend.modules.clinical.repository.LabTechnicianRepository;
import com.lumina.backend.modules.medicalrecord.entity.MedicalResult;
import com.lumina.backend.modules.medicalrecord.entity.Prescription;
import com.lumina.backend.modules.medicalrecord.repository.MedicalResultRepository;
import com.lumina.backend.modules.medicalrecord.repository.PrescriptionRepository;
import com.lumina.backend.modules.patient.entity.Patient;
import com.lumina.backend.modules.patient.repository.PatientRepository;
import com.lumina.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MedicalRecordService {

    private final PrescriptionRepository prescriptionRepository;
    private final MedicalResultRepository medicalResultRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final LabTechnicianRepository labTechnicianRepository;


    public Prescription addPrescription(PrescriptionRequest request) {
        Doctor doctor = getCurrentDoctor();
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Prescription prescription = Prescription.builder()
                .patient(patient)
                .doctor(doctor)
                .medicationName(request.getMedicationName())
                .dosage(request.getDosage())
                .administrationTimes(request.getAdministrationTimes())
                .frequencyDays(request.getFrequencyDays())
                .startDate(request.getStartDate())
                .durationInDays(request.getDurationInDays())
                .endDate(request.getStartDate().plusDays(request.getDurationInDays()))
                .instructions(request.getInstructions())
                .build();

        return prescriptionRepository.save(prescription);
    }

    public MedicalResult addMedicalResult(MedicalResultRequest request) {
        LabTechnician technician = getCurrentLabTechnician();

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        MedicalResult result = MedicalResult.builder()
                .patient(patient)
                .technician(technician)
                .resultType(request.getResultType())
                .description(request.getDescription())
                .fileUrl(request.getFileUrl())
                .uploadedAt(LocalDateTime.now())
                .build();

        return medicalResultRepository.save(result);
    }

    // Helper method
    private LabTechnician getCurrentLabTechnician() {
        User user = getCurrentUser();
        return labTechnicianRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user is not a Lab Technician"));
    }

    public void updatePatientVitals(Long patientId, Double height, Double weight, String bloodType) {
        // This can be used by Doctor to update patient info
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        if(height != null) patient.setHeight(height);
        if(weight != null) patient.setWeight(weight);
        if(bloodType != null) patient.setBloodType(bloodType);

        patientRepository.save(patient);
    }

    public List<PrescriptionResponse> getMyPrescriptions() {
        User user = getCurrentUser();
        return prescriptionRepository.findByPatientId(user.getId())
                .stream()
                .map(p -> PrescriptionResponse.builder()
                        .id(p.getId())
                        .medicationName(p.getMedicationName())
                        .dosage(p.getDosage())
                        .administrationTimes(p.getAdministrationTimes())
                        .frequencyDays(p.getFrequencyDays())
                        .startDate(p.getStartDate())
                        .endDate(p.getEndDate())
                        .durationInDays(p.getDurationInDays())
                        .instructions(p.getInstructions())
                        .doctorName(p.getDoctor().getFullName())
                        .doctorId(p.getDoctor().getId())
                        .build())
                .toList();
    }

    public List<MedicalResultResponse> getMyResults() {
        User user = getCurrentUser();
        return medicalResultRepository.findByPatientId(user.getId())
                .stream()
                .map(r -> MedicalResultResponse.builder()
                        .id(r.getId())
                        .resultType(r.getResultType())
                        .description(r.getDescription())
                        .fileUrl(r.getFileUrl())
                        .uploadedAt(r.getUploadedAt())
                        .technicianName(r.getTechnician() != null ? r.getTechnician().getFullName() : "Unknown")
                        .build())
                .toList();
    }


    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Doctor getCurrentDoctor() {
        User user = getCurrentUser();
        return doctorRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user is not a doctor"));
    }
}