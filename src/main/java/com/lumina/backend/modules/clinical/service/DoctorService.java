package com.lumina.backend.modules.clinical.service;

import com.lumina.backend.modules.auth.dto.ChangePasswordRequest;
import com.lumina.backend.modules.clinical.dto.DoctorRegisterRequest;
import com.lumina.backend.modules.clinical.dto.DoctorRequest;
import com.lumina.backend.modules.clinical.dto.DoctorResponse;
import com.lumina.backend.modules.appointment.entity.Appointment;
import com.lumina.backend.modules.clinical.entity.Doctor;

import java.time.LocalDate;
import java.util.List;

public interface DoctorService {
    List<DoctorResponse> getAllDoctors(Long branchId);

    DoctorResponse getDoctorById(Long id);
    DoctorResponse createDoctor(DoctorRegisterRequest request);

    void deleteDoctor(Long id);

    DoctorResponse updateDoctor(Long id, DoctorRequest doctorDetails);

    List<Appointment> getDoctorAppointmentsByDate(Long doctorId, LocalDate date);

    void validateDoctorOwnership(Long doctorId, String email);

    Doctor findDoctorEntityById(Long id);
    void changePassword(Long doctorId, ChangePasswordRequest request);
}