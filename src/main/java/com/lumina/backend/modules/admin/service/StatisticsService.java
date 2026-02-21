package com.lumina.backend.modules.admin.service;

import com.lumina.backend.modules.admin.dto.AdminDashboardResponse;
import com.lumina.backend.modules.admin.dto.TopDoctor;
import com.lumina.backend.modules.appointment.repository.AppointmentRepository;
import com.lumina.backend.modules.clinical.entity.Doctor;
import com.lumina.backend.modules.clinical.repository.DoctorRepository;
import com.lumina.backend.modules.operations.dto.BranchDensityResponse;
import com.lumina.backend.modules.operations.entity.Branch;
import com.lumina.backend.modules.operations.repository.BranchRepository;
import com.lumina.backend.modules.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private static final double EXAMINATION_FEE = 750.0;
    private final BranchRepository branchRepository;

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboardStats() {
        LocalDateTime now = LocalDateTime.now();

        long totalPatients = patientRepository.count();
        long totalDoctors = doctorRepository.count();
        long totalAppointments = appointmentRepository.count();

        LocalDateTime startOfThisMonth = now.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
        long appointmentsThisMonth = appointmentRepository.countByAppointmentTimeBetween(startOfThisMonth, now);

        double monthlyEarnings = appointmentsThisMonth * EXAMINATION_FEE;

        List<Integer> chartData = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthDate = now.minusMonths(i);
            LocalDateTime start = monthDate.with(TemporalAdjusters.firstDayOfMonth()).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime end = monthDate.with(TemporalAdjusters.lastDayOfMonth()).withHour(23).withMinute(59).withSecond(59);
            long count = appointmentRepository.countByAppointmentTimeBetween(start, end);
            chartData.add((int) count);
            labels.add(monthDate.getMonth().getDisplayName(TextStyle.SHORT, new Locale("tr")));
        }

        // Top 5 Doctors Logic
        List<Object[]> topDocsRaw = appointmentRepository.findTopDoctors(PageRequest.of(0, 5));
        List<TopDoctor> topDoctors = topDocsRaw.stream().map(obj -> {
            Doctor doctor = (Doctor) obj[0];
            Long count = (Long) obj[1];
            return TopDoctor.builder()
                    .name(doctor.getFullName())
                    .branch(doctor.getSpecialty())
                    .appointmentCount(count)
                    .imageUrl(doctor.getImageUrl())
                    .rating(doctor.getRating())
                    .build();
        }).toList();

        return AdminDashboardResponse.builder()
                .totalPatients(totalPatients)
                .totalDoctors(totalDoctors)
                .totalAppointments(totalAppointments)
                .monthlyEarnings(monthlyEarnings)
                .monthlyAppointmentsData(chartData)
                .monthLabels(labels)
                .topDoctors(topDoctors)
                .build();
    }


    public List<BranchDensityResponse> getPolyclinicDensity() {
        List<Branch> branches = branchRepository.findAll();
        List<BranchDensityResponse> densityList = new ArrayList<>();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        for (Branch branch : branches) {
             List<Doctor> doctors = doctorRepository.findByBranchId(branch.getId());
             long appointmentCount = appointmentRepository.countByDoctorBranchIdAndAppointmentTimeBetween(
                    branch.getId(), startOfDay, endOfDay
            );
             long totalCapacity = doctors.size() * 20L;

            double occupancyRate = totalCapacity > 0 ? ((double) appointmentCount / totalCapacity) * 100 : 0;

            densityList.add(BranchDensityResponse.builder()
                    .branchName(branch.getName())
                    .totalAppointmentsToday(appointmentCount)
                    .activeDoctors((long) doctors.size())
                    .occupancyRate(Math.round(occupancyRate * 10.0) / 10.0)
                    .build());
        }
        return densityList;
    }
}