package com.lumina.backend.modules.admin.service;

import com.lumina.backend.modules.admin.dto.AdminDashboardResponse;
import com.lumina.backend.modules.appointment.repository.AppointmentRepository;
import com.lumina.backend.modules.auth.entity.User;
import com.lumina.backend.modules.clinical.entity.Doctor;
import com.lumina.backend.modules.clinical.repository.DoctorRepository;
import com.lumina.backend.modules.operations.dto.BranchDensityResponse;
import com.lumina.backend.modules.operations.entity.Branch;
import com.lumina.backend.modules.operations.repository.BranchRepository;
import com.lumina.backend.modules.patient.repository.PatientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatisticsServiceTest {

    @Mock private PatientRepository patientRepository;
    @Mock private DoctorRepository doctorRepository;
    @Mock private AppointmentRepository appointmentRepository;
    @Mock private BranchRepository branchRepository;

    @InjectMocks
    private StatisticsService statisticsService;

    @Test
    @DisplayName("getDashboardStats - Should calculate totals and map top doctors")
    void getDashboardStats_ShouldReturnCorrectData() {
          
        when(patientRepository.count()).thenReturn(100L);
        when(doctorRepository.count()).thenReturn(20L);
        when(appointmentRepository.count()).thenReturn(500L);

          
        when(appointmentRepository.countByAppointmentTimeBetween(any(), any()))
                .thenReturn(5L);   

          
          
        User doctorUser = User.builder()
                .fullName("Dr. Heart")
                .build();

        Doctor doc = Doctor.builder()
                .id(1L)
                .user(doctorUser)   
                .specialty("Cardiology")
                .title("Dr")
                .build();

          
        List<Object[]> topDocsRaw = new ArrayList<>();
        topDocsRaw.add(new Object[]{doc, 15L});

        when(appointmentRepository.findTopDoctors(any(Pageable.class))).thenReturn(topDocsRaw);

          
        AdminDashboardResponse response = statisticsService.getDashboardStats();

          
        assertThat(response).isNotNull();
        assertThat(response.getTotalPatients()).isEqualTo(100L);
        assertThat(response.getTotalDoctors()).isEqualTo(20L);
        assertThat(response.getTotalAppointments()).isEqualTo(500L);
        assertThat(response.getMonthlyEarnings()).isEqualTo(3750.0);

          
        assertThat(response.getMonthlyAppointmentsData()).hasSize(6);
        assertThat(response.getMonthLabels()).hasSize(6);

          
        assertThat(response.getTopDoctors()).hasSize(1);
          
        assertThat(response.getTopDoctors().get(0).getName()).isEqualTo("Dr. Heart");
        assertThat(response.getTopDoctors().get(0).getAppointmentCount()).isEqualTo(15L);
    }

    @Test
    @DisplayName("getPolyclinicDensity - Should calculate occupancy rates correctly")
    void getPolyclinicDensity_ShouldReturnDensityList() {
          
        Long branchId = 1L;
        Branch branch = Branch.builder().id(branchId).name("Neurology").build();

          
        List<Doctor> doctors = List.of(new Doctor(), new Doctor());

        when(branchRepository.findAll()).thenReturn(List.of(branch));
        when(doctorRepository.findByBranchId(branchId)).thenReturn(doctors);

          
        when(appointmentRepository.countByDoctorBranchIdAndAppointmentTimeBetween(any(), any(), any()))
                .thenReturn(10L);

          
        List<BranchDensityResponse> result = statisticsService.getPolyclinicDensity();

          
          
        assertThat(result).hasSize(1);
        BranchDensityResponse density = result.get(0);
        assertThat(density.getBranchName()).isEqualTo("Neurology");
        assertThat(density.getTotalAppointmentsToday()).isEqualTo(10L);
        assertThat(density.getActiveDoctors()).isEqualTo(2L);
        assertThat(density.getOccupancyRate()).isEqualTo(25.0);
    }

    @Test
    @DisplayName("getPolyclinicDensity - Should handle zero capacity (no doctors) gracefully")
    void getPolyclinicDensity_ShouldHandleZeroCapacity() {
          
        Branch branch = Branch.builder().id(2L).name("Empty Branch").build();
        when(branchRepository.findAll()).thenReturn(List.of(branch));
        when(doctorRepository.findByBranchId(2L)).thenReturn(Collections.emptyList());   
        when(appointmentRepository.countByDoctorBranchIdAndAppointmentTimeBetween(any(), any(), any()))
                .thenReturn(0L);

          
        List<BranchDensityResponse> result = statisticsService.getPolyclinicDensity();

          
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOccupancyRate()).isEqualTo(0.0);
    }
}