package com.lumina.backend.modules.appointment.service;

import com.lumina.backend.modules.appointment.dto.AppointmentRequest;
import com.lumina.backend.modules.appointment.dto.AppointmentResponse;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentService {
    AppointmentResponse createAppointment(AppointmentRequest request);

    AppointmentResponse updateAppointment(Long id, AppointmentRequest request);

    List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId);

    List<AppointmentResponse> getAppointmentsByDoctorAndDate(Long doctorId, LocalDate date);

    List<AppointmentResponse> getMyAppointments();

    void deleteAppointment(Long id);

    List<AppointmentResponse> getAllAppointments();

    AppointmentResponse getAppointmentById(Long id);

    List<AppointmentResponse> getIncomingAppointmentsForCurrentDoctor(LocalDate date);
}