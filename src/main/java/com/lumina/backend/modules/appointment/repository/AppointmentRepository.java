package com.lumina.backend.modules.appointment.repository;

import com.lumina.backend.modules.operations.dto.BranchStatsProjection;
import com.lumina.backend.modules.appointment.entity.Appointment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientIdOrderByAppointmentTimeAsc(Long patientId);
    List<Appointment> findByDoctorId(Long doctorId);

    boolean existsByPatientIdAndDoctorIdAndAppointmentTimeAfter(Long patientId, Long doctorId, LocalDateTime now);

    List<Appointment> findByDoctorIdAndAppointmentTimeBetween(Long doctorId, LocalDateTime start, LocalDateTime end);
    long countByDoctorBranchIdAndAppointmentTimeBetween(Long branchId, LocalDateTime start, LocalDateTime end);
    long countByAppointmentTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT a.doctor, COUNT(a) FROM Appointment a " +
            "JOIN a.doctor d " +
            "GROUP BY a.doctor " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> findTopDoctors(Pageable pageable);

    @Query("SELECT b.name as branchName, COUNT(a) as count " +
            "FROM Appointment a " +
            "JOIN a.doctor d " +
            "JOIN d.branch b " +
            "GROUP BY b.name")
    List<BranchStatsProjection> countAppointmentsByBranch();

    boolean existsByDoctorIdAndAppointmentTime(Long doctorId, LocalDateTime appointmentTime);
    boolean existsByPatientIdAndAppointmentTime(Long patientId, LocalDateTime appointmentTime);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.appointmentTime BETWEEN :start AND :end")
    long countAppointmentsInYear(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}