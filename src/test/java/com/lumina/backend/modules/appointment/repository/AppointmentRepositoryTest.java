package com.lumina.backend.modules.appointment.repository;

import com.lumina.backend.modules.appointment.entity.Appointment;
import com.lumina.backend.modules.auth.entity.Role;
import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.auth.repository.UserRepository;
import com.lumina.backend.modules.clinical.entity.Doctor;
import com.lumina.backend.modules.clinical.repository.DoctorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("existsByDoctorIdAndAppointmentTime should return true if slot is taken")
    void shouldCheckIfDoctorSlotIsTaken() {
        User doctorUser = User.builder()
                .email("doctor@test.com")
                .fullName("Dr. Strange")
                .password("encodedPass")
                .role(Role.DOCTOR)
                .build();
        userRepository.save(doctorUser);

        Doctor doctor = Doctor.builder()
                .user(doctorUser)
                .title("Dr.")
                .diplomaNo("DIP123456")
                .specialty("Magic")
                .build();
        doctor = doctorRepository.save(doctor);

        LocalDateTime time = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        Appointment appointment = Appointment.builder()
                .doctor(doctor)
                .appointmentTime(time)
                .isAvailable(false)
                .build();
        appointmentRepository.save(appointment);

        boolean exists = appointmentRepository.existsByDoctorIdAndAppointmentTime(doctor.getId(), time);
        boolean notExists = appointmentRepository.existsByDoctorIdAndAppointmentTime(doctor.getId(), time.plusHours(1));

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    @DisplayName("findTopDoctors - Should return doctors ordered by appointment count")
    void shouldReturnTopDoctors() {
        User u1 = userRepository.save(User.builder().email("d1@t.com").password("pw").fullName("D1").build());
        Doctor d1 = doctorRepository.save(Doctor.builder().user(u1).title("Dr").specialty("A").build());

        User u2 = userRepository.save(User.builder().email("d2@t.com").password("pw").fullName("D2").build());
        Doctor d2 = doctorRepository.save(Doctor.builder().user(u2).title("Dr").specialty("B").build());

        LocalDateTime time1 = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime time2 = LocalDateTime.now().plusHours(1).truncatedTo(ChronoUnit.SECONDS);

        appointmentRepository.save(Appointment.builder().doctor(d1).appointmentTime(time1).build());
        appointmentRepository.save(Appointment.builder().doctor(d1).appointmentTime(time2).build());

        List<Object[]> results = appointmentRepository.findTopDoctors(org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(results).isNotEmpty();
        Doctor topDoctor = (Doctor) results.get(0)[0];
        Long count = (Long) results.get(0)[1];

        assertThat(topDoctor.getId()).isEqualTo(d1.getId());
        assertThat(count).isEqualTo(2L);
    }

    @Test
    @DisplayName("findByDoctorIdAndAppointmentTimeBetween - Should filter by date correctly")
    void shouldFilterAppointmentsByDateRange() {
        User u = userRepository.save(User.builder().email("doc@t.com").password("pw").fullName("Doc").build());
        Doctor doc = doctorRepository.save(Doctor.builder().user(u).title("Dr").specialty("X").build());

        LocalDateTime today = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        LocalDateTime tomorrow = today.plusDays(1);

        appointmentRepository.save(Appointment.builder().doctor(doc).appointmentTime(today).build());
        appointmentRepository.save(Appointment.builder().doctor(doc).appointmentTime(tomorrow).build());

        LocalDateTime start = today.minusMinutes(1);
        LocalDateTime end = today.plusMinutes(1);

        List<Appointment> result = appointmentRepository.findByDoctorIdAndAppointmentTimeBetween(doc.getId(), start, end);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAppointmentTime()).isEqualTo(today);
    }

    @Test
    @DisplayName("existsByPatientIdAndDoctorIdAndAppointmentTimeAfter - Should prevent duplicate active bookings")
    void shouldCheckDuplicateActiveBookings() {
        User u1 = userRepository.save(User.builder().email("p@t.com").password("pw").fullName("P1").build());
        User u2 = userRepository.save(User.builder().email("d@t.com").password("pw").fullName("D1").build());
        Doctor doc = doctorRepository.save(Doctor.builder().user(u2).title("Dr").specialty("X").build());

        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(5).truncatedTo(ChronoUnit.SECONDS);

        appointmentRepository.save(Appointment.builder()
                .patient(u1)
                .doctor(doc)
                .appointmentTime(appointmentTime)
                .isAvailable(false)
                .build());

        boolean hasUpcoming = appointmentRepository.existsByPatientIdAndDoctorIdAndAppointmentTimeAfter(
                u1.getId(),
                doc.getId(),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        assertThat(hasUpcoming).isTrue();
    }

    @Test
    @DisplayName("countAppointmentsByBranch - Should return correct statistics")
    void shouldCountAppointmentsByBranch() {
    }
}