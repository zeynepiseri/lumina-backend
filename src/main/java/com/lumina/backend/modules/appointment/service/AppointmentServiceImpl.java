package com.lumina.backend.modules.appointment.service;

import com.lumina.backend.modules.appointment.dto.AppointmentRequest;
import com.lumina.backend.modules.appointment.dto.AppointmentResponse;
import com.lumina.backend.modules.appointment.entity.Appointment;
import com.lumina.backend.modules.clinical.entity.Doctor;
import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.operations.service.NotificationService;
import com.lumina.backend.shared.audit.AuditLogService;
import com.lumina.backend.shared.exception.BusinessException;
import com.lumina.backend.shared.exception.ResourceNotFoundException;
import com.lumina.backend.modules.appointment.repository.AppointmentRepository;
import com.lumina.backend.modules.clinical.repository.DoctorRepository;
import com.lumina.backend.modules.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    @Override
    public AppointmentResponse createAppointment(AppointmentRequest request) {
         
        if (request.getAppointmentTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Cannot create an appointment for a past date.");
        }

         
        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User patient = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateDoctorAvailability(doctor, request.getAppointmentTime());

        boolean isPatientBusy = appointmentRepository.existsByPatientIdAndAppointmentTime(
                patient.getId(),
                request.getAppointmentTime()
        );
        if (isPatientBusy) {
            throw new BusinessException("You already have an appointment scheduled at " + request.getAppointmentTime().toLocalTime() + ".");
        }

        boolean hasUpcoming = appointmentRepository.existsByPatientIdAndDoctorIdAndAppointmentTimeAfter(
                patient.getId(),
                doctor.getId(),
                LocalDateTime.now()
        );

        if (hasUpcoming) {
            throw new BusinessException("You already have an upcoming appointment with this doctor.");
        }

        Appointment appointment = Appointment.builder()
                .appointmentTime(request.getAppointmentTime())
                .doctor(doctor)
                .patient(patient)
                .isAvailable(false)
                .healthIssue(request.getHealthIssue())
                .appointmentType(request.getAppointmentType())
                .consultationMethod(request.getConsultationMethod())
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

        notificationService.sendNotification(
                savedAppointment.getPatient(),
                "Appointment Confirmed",
                "Dear " + savedAppointment.getPatient().getFullName() + ", your appointment on " +
                        savedAppointment.getAppointmentTime() + " has been confirmed.",
                "APPOINTMENT"
        );

        notificationService.sendNotification(
                savedAppointment.getDoctor().getUser(),
                "New Appointment",
                "New patient appointment: " + savedAppointment.getPatient().getFullName() +
                        " (" + savedAppointment.getAppointmentTime() + ")",
                "APPOINTMENT"
        );

        auditLogService.log("CREATE_APPOINTMENT",
                "Appointment created. ID: " + savedAppointment.getId() + " - Patient: " + savedAppointment.getPatient().getEmail());

        return mapToResponse(savedAppointment);
    }

    private void validateDoctorAvailability(Doctor doctor, LocalDateTime appointmentTime) {
        boolean isSlotTaken = appointmentRepository.existsByDoctorIdAndAppointmentTime(doctor.getId(), appointmentTime);
        if (isSlotTaken) {
            throw new BusinessException("The doctor is not available at the selected time.");
        }
        DayOfWeek dayOfWeek = appointmentTime.getDayOfWeek();
        LocalTime time = appointmentTime.toLocalTime();

        if (doctor.getSchedules() == null) return;

        boolean isWorkingHour = doctor.getSchedules().stream()
                .anyMatch(schedule ->
                        schedule.getDayOfWeek() == dayOfWeek &&
                                !time.isBefore(schedule.getStartTime()) &&
                                time.isBefore(schedule.getEndTime())
                );

        if (!isWorkingHour) {
            throw new BusinessException("The doctor is not working on this date and time.");
        }
    }

    @Override
    public List<AppointmentResponse> getMyAppointments() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return appointmentRepository.findByPatientIdOrderByAppointmentTimeAsc(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponse> getIncomingAppointmentsForCurrentDoctor(LocalDate date) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Doctor doctor = doctorRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current user is not a doctor"));

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctor.getId(), startOfDay, endOfDay)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponse> getAppointmentsByDoctor(Long doctorId) {
        return appointmentRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<AppointmentResponse> getAppointmentsByDoctorAndDate(Long doctorId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doctorId, startOfDay, endOfDay)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentResponse updateAppointment(Long id, AppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found"));

        if (request.getAppointmentTime() != null) {
            validateDoctorAvailability(appointment.getDoctor(), request.getAppointmentTime());

            if (!request.getAppointmentTime().equals(appointment.getAppointmentTime())) {
                boolean isPatientBusy = appointmentRepository.existsByPatientIdAndAppointmentTime(
                        appointment.getPatient().getId(),
                        request.getAppointmentTime()
                );
                if (isPatientBusy) {
                    throw new BusinessException("You already have another appointment at " + request.getAppointmentTime().toLocalTime() + ".");
                }
            }

            appointment.setAppointmentTime(request.getAppointmentTime());
        }
        if (request.getHealthIssue() != null) appointment.setHealthIssue(request.getHealthIssue());
        if (request.getAppointmentType() != null) appointment.setAppointmentType(request.getAppointmentType());
        if (request.getConsultationMethod() != null) appointment.setConsultationMethod(request.getConsultationMethod());

        Appointment updated = appointmentRepository.save(appointment);
        return mapToResponse(updated);
    }

    @Override
    public List<AppointmentResponse> getAllAppointments() {
        return appointmentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
        return mapToResponse(appointment);
    }

    @Override
    public void deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Appointment not found with id: " + id);
        }
        appointmentRepository.deleteById(id);
    }

    private AppointmentResponse mapToResponse(Appointment appointment) {
        return AppointmentResponse.builder()
                .id(appointment.getId())
                .appointmentTime(appointment.getAppointmentTime())
                .isAvailable(appointment.getIsAvailable())
                .status(appointment.getAppointmentTime().isBefore(LocalDateTime.now()) ? "Completed" : "Upcoming")
                .doctorId(appointment.getDoctor().getId())
                .doctorName(appointment.getDoctor().getFullName())
                .doctorTitle(appointment.getDoctor().getTitle())
                .doctorSpecialty(appointment.getDoctor().getSpecialty())
                .doctorImageUrl(appointment.getDoctor().getImageUrl())
                .patientId(appointment.getPatient() != null ? appointment.getPatient().getId() : null)
                .patientName(appointment.getPatient() != null ? appointment.getPatient().getFullName() : "Unknown")
                .healthIssue(appointment.getHealthIssue())
                .appointmentType(appointment.getAppointmentType())
                .consultationMethod(appointment.getConsultationMethod())
                .build();
    }
}