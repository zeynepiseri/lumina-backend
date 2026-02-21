package com.lumina.backend.modules.appointment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.backend.modules.appointment.dto.AppointmentRequest;
import com.lumina.backend.modules.appointment.dto.AppointmentResponse;
import com.lumina.backend.modules.appointment.service.AppointmentService;
import com.lumina.backend.infrastructure.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
@AutoConfigureMockMvc
@WithMockUser
 
@Import(AppointmentControllerTest.TestSecurityConfig.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private JwtService jwtService;

    @TestConfiguration
    @EnableMethodSecurity  
    static class TestSecurityConfig {
    }

    @Test
    @DisplayName("POST /appointments - Should create appointment successfully")
    void createAppointment_ShouldReturnCreated() throws Exception {
        AppointmentRequest request = new AppointmentRequest();
        request.setDoctorId(1L);
        request.setAppointmentTime(LocalDateTime.now().plusDays(1));
        request.setHealthIssue("Migraine");

        AppointmentResponse response = AppointmentResponse.builder()
                .id(10L)
                .status("Upcoming")
                .doctorName("Dr. House")
                .build();

        given(appointmentService.createAppointment(any(AppointmentRequest.class))).willReturn(response);

        mockMvc.perform(post("/appointments")
                        .with(csrf())  
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.doctorName").value("Dr. House"));
    }

    @Test
    @DisplayName("GET /appointments/my - Should return user's appointments")
    void getMyAppointments_ShouldReturnList() throws Exception {
        AppointmentResponse app1 = AppointmentResponse.builder().id(1L).status("Upcoming").build();
        given(appointmentService.getMyAppointments()).willReturn(List.of(app1));

        mockMvc.perform(get("/appointments/my")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("PUT /appointments/{id} - Should update appointment")
    void updateAppointment_ShouldReturnUpdated() throws Exception {
        Long id = 1L;
        AppointmentRequest request = new AppointmentRequest();
        request.setHealthIssue("Updated Issue");

        AppointmentResponse response = AppointmentResponse.builder()
                .id(id)
                .healthIssue("Updated Issue")
                .build();

        given(appointmentService.updateAppointment(eq(id), any(AppointmentRequest.class))).willReturn(response);

        mockMvc.perform(put("/appointments/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthIssue").value("Updated Issue"));
    }

    @Test
    @DisplayName("DELETE /appointments/{id} - Should delete appointment")
    void deleteAppointment_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/appointments/{id}", 1L)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /by-date - Should forbid non-doctor users")
    @WithMockUser(username = "patient", roles = {"PATIENT"})  
    void getDoctorAppointmentsByDate_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/appointments/by-date")
                        .param("date", LocalDate.now().toString()))
                .andExpect(status().isForbidden());  
    }

    @Test
    @DisplayName("GET /by-date - Should allow doctors")
    @WithMockUser(username = "doctor", roles = {"DOCTOR"})
    void getDoctorAppointmentsByDate_ShouldReturnOk() throws Exception {
        LocalDate today = LocalDate.now();

        given(appointmentService.getIncomingAppointmentsForCurrentDoctor(today))
                .willReturn(List.of());

        mockMvc.perform(get("/appointments/by-date")
                        .param("date", today.toString()))
                .andExpect(status().isOk());
    }
}