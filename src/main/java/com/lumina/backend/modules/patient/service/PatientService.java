package com.lumina.backend.modules.patient.service;

import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.auth.repository.UserRepository;
import com.lumina.backend.modules.patient.dto.PatientResponse;
import com.lumina.backend.modules.patient.entity.Patient;
import com.lumina.backend.modules.patient.repository.PatientRepository;
import com.lumina.backend.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    public Page<PatientResponse> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    public PatientResponse getPatientById(Long id) {
        Patient patient = findPatientEntityById(id);
        return mapToResponse(patient);
    }

    public void updatePatientVitals(Long id, Double height, Double weight, String bloodType) {
        Patient patient = findPatientEntityById(id);

        if (height != null) patient.setHeight(height);
        if (weight != null) patient.setWeight(weight);
        if (bloodType != null) patient.setBloodType(bloodType);

        patientRepository.save(patient);
    }

    private Patient findPatientEntityById(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));
    }

    public PatientResponse getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Patient patient = patientRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found"));

        return mapToResponse(patient);
    }

    public void updateMyVitals(String email, Double height, Double weight, String bloodType) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        updatePatientVitals(user.getId(), height, weight, bloodType);
    }

    private PatientResponse mapToResponse(Patient patient) {
        User user = patient.getUser();
        return PatientResponse.builder()
                .id(patient.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .nationalId(user.getNationalId())
                .imageUrl(user.getImageUrl())
                .gender(user.getGender())
                .birthDate(user.getBirthDate())
                .height(patient.getHeight())
                .weight(patient.getWeight())
                .bloodType(patient.getBloodType())
                .allergies(patient.getAllergies() != null ? new ArrayList<>(patient.getAllergies()) : new ArrayList<>())
                .chronicDiseases(patient.getChronicDiseases() != null ? new ArrayList<>(patient.getChronicDiseases()) : new ArrayList<>())
                .build();
    }
}