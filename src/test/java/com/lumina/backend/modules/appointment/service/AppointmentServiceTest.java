package com.lumina.backend.modules.appointment.service;

import com.lumina.backend.modules.appointment.dto.AppointmentRequest;
import com.lumina.backend.modules.appointment.dto.AppointmentResponse;
import com.lumina.backend.modules.appointment.entity.Appointment;
import com.lumina.backend.modules.appointment.repository.AppointmentRepository;
import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.auth.repository.UserRepository;
import com.lumina.backend.modules.clinical.entity.Doctor;
import com.lumina.backend.modules.clinical.entity.DoctorSchedule;
import com.lumina.backend.modules.clinical.repository.DoctorRepository;
import com.lumina.backend.modules.operations.service.NotificationService;
import com.lumina.backend.shared.audit.AuditLogService;
import com.lumina.backend.shared.exception.BusinessException;
import com.lumina.backend.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock private AppointmentRepository appointmentRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private UserRepository userRepository;
    @Mock private NotificationService notificationService;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private MockedStatic<SecurityContextHolder> mockedSecurityContextHolder;
    private Authentication authentication;
    private SecurityContext securityContext;

    @BeforeEach
    void setUp() {
        mockedSecurityContextHolder = Mockito.mockStatic(SecurityContextHolder.class, Mockito.withSettings().lenient());
        securityContext = mock(SecurityContext.class);
        authentication = mock(Authentication.class);

        mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    @AfterEach
    void tearDown() {
        mockedSecurityContextHolder.close();
    }

    @Test
    @DisplayName("Should create appointment successfully when all conditions are met")
    void shouldCreateAppointmentSuccessfully() {
        Long doctorId = 1L;
        String patientEmail = "patient@lumina.com";
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        DayOfWeek dayOfWeek = appointmentTime.getDayOfWeek();

        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(doctorId);
        request.setAppointmentTime(appointmentTime);
        request.setHealthIssue("Checkup");
        request.setAppointmentType("On-site");
        request.setConsultationMethod("Face-to-Face");

        User patientUser = User.builder().id(100L).email(patientEmail).fullName("John Doe").build();

        User doctorUser = User.builder()
                .email("house@lumina.com")
                .fullName("Dr. House")
                .build();

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDayOfWeek(dayOfWeek);
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(17, 0));

        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .user(doctorUser)
                .schedules(List.of(schedule))
                .build();
        schedule.setDoctor(doctor);

        when(authentication.getName()).thenReturn(patientEmail);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(userRepository.findByEmail(patientEmail)).thenReturn(Optional.of(patientUser));
        when(appointmentRepository.existsByDoctorIdAndAppointmentTime(doctorId, appointmentTime)).thenReturn(false);
        when(appointmentRepository.existsByPatientIdAndAppointmentTime(patientUser.getId(), appointmentTime)).thenReturn(false);
        when(appointmentRepository.existsByPatientIdAndDoctorIdAndAppointmentTimeAfter(any(), any(), any())).thenReturn(false);

        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment a = invocation.getArgument(0);
            a.setId(1L);
            return a;
        });

        AppointmentResponse response = appointmentService.createAppointment(request);

        assertThat(response).isNotNull();
        assertThat(response.getDoctorName()).isEqualTo("Dr. House");
        assertThat(response.getStatus()).isEqualTo("Upcoming");

        verify(notificationService, times(2)).sendNotification(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when doctor does not exist")
    void shouldThrowExceptionWhenDoctorNotFound() {
        Long nonExistentDoctorId = 999L;

        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(nonExistentDoctorId);
        request.setAppointmentTime(LocalDateTime.now().plusDays(1));

        when(doctorRepository.findById(nonExistentDoctorId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(com.lumina.backend.shared.exception.ResourceNotFoundException.class)
                .hasMessageContaining("Doctor not found");

        verify(appointmentRepository, never()).save(any());
        verify(notificationService, never()).sendNotification(any(), any(), any(), any());
    }

    @Test
    @DisplayName("getIncomingAppointments - Should return appointments when user is a doctor")
    void shouldReturnIncomingAppointmentsForDoctor() {
        String doctorEmail = "house@lumina.com";
        LocalDate today = LocalDate.now();
        Long doctorId = 55L;

        User doctorUser = User.builder().id(doctorId).email(doctorEmail).build();
        Doctor doctor = Doctor.builder().id(doctorId).user(doctorUser).build();

        when(authentication.getName()).thenReturn(doctorEmail);
        when(userRepository.findByEmail(doctorEmail)).thenReturn(Optional.of(doctorUser));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        Appointment app1 = Appointment.builder()
                .id(1L)
                .appointmentTime(LocalDateTime.now())
                .doctor(doctor)
                .patient(User.builder().fullName("Patient A").build())
                .isAvailable(false)
                .build();

        when(appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(
                eq(doctorId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(app1));

        List<AppointmentResponse> results = appointmentService.getIncomingAppointmentsForCurrentDoctor(today);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDoctorId()).isEqualTo(doctorId);
    }

    @Test
    @DisplayName("updateAppointment - Should throw exception if rescheduling to a busy slot")
    void shouldThrowException_WhenReschedulingToBusySlot() {
        Long appointmentId = 1L;
        Long doctorId = 2L;
        LocalDateTime originalTime = LocalDateTime.now().plusDays(1).withHour(10);
        LocalDateTime newTime = LocalDateTime.now().plusDays(1).withHour(11);

        Appointment existingAppointment = Appointment.builder()
                .id(appointmentId)
                .appointmentTime(originalTime)
                .doctor(Doctor.builder().id(doctorId).build())
                .patient(User.builder().id(100L).build())
                .build();

        AppointmentRequest updateRequest = new AppointmentRequest();
        updateRequest.setAppointmentTime(newTime);

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(existingAppointment));
        when(appointmentRepository.existsByDoctorIdAndAppointmentTime(doctorId, newTime)).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.updateAppointment(appointmentId, updateRequest))
                .isInstanceOf(com.lumina.backend.shared.exception.BusinessException.class)
                .hasMessageContaining("doctor is not available");
    }

    @Test
    @DisplayName("Create - Should throw exception if appointment is in the past")
    void shouldThrowException_WhenAppointmentIsInPast() {
        AppointmentRequest request = new AppointmentRequest();
        request.setAppointmentTime(LocalDateTime.now().minusHours(1));

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Cannot create an appointment for a past date");
    }

    @Test
    @DisplayName("Create - Should throw exception if doctor is not working at that time (Schedule Check)")
    void shouldThrowException_WhenDoctorIsNotWorking() {
        Long doctorId = 1L;
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1).withHour(20);

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDayOfWeek(appointmentTime.getDayOfWeek());
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(17, 0));

        Doctor doctor = Doctor.builder()
                .id(doctorId)
                .schedules(List.of(schedule))
                .build();

        schedule.setDoctor(doctor);

        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(doctorId);
        request.setAppointmentTime(appointmentTime);

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(authentication.getName()).thenReturn("patient@lumina.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(com.lumina.backend.shared.exception.BusinessException.class)
                .hasMessageContaining("doctor is not working");
    }

    @Test
    @DisplayName("Create - Should throw exception if patient is double-booked")
    void shouldThrowException_WhenPatientIsBusy() {
        Long doctorId = 1L;
        Long patientId = 100L;
        LocalDateTime time = LocalDateTime.now().plusDays(1).withHour(10);

        Doctor doctor = Doctor.builder().id(doctorId).schedules(List.of(createSchedule(time))).build();
        User patient = User.builder().id(patientId).email("p@l.com").build();

        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(doctorId);
        request.setAppointmentTime(time);

        when(authentication.getName()).thenReturn("p@l.com");
        when(userRepository.findByEmail("p@l.com")).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));

        when(appointmentRepository.existsByPatientIdAndAppointmentTime(patientId, time)).thenReturn(true);

        assertThatThrownBy(() -> appointmentService.createAppointment(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already have an appointment scheduled");
    }

    @Test
    @DisplayName("Delete - Should throw exception if appointment does not exist")
    void shouldThrowException_WhenDeletingNonExistentAppointment() {
        Long invalidId = 999L;
        when(appointmentRepository.existsById(invalidId)).thenReturn(false);

        assertThatThrownBy(() -> appointmentService.deleteAppointment(invalidId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(appointmentRepository, never()).deleteById(any());
    }

     
    private DoctorSchedule createSchedule(LocalDateTime time) {
        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setDayOfWeek(time.getDayOfWeek());
        schedule.setStartTime(LocalTime.of(8, 0));
        schedule.setEndTime(LocalTime.of(18, 0));
        return schedule;
    }
}