package com.lumina.backend.modules.clinical.controller;

import com.lumina.backend.modules.appointment.service.AppointmentService;
import com.lumina.backend.modules.clinical.dto.DoctorResponse;
import com.lumina.backend.modules.clinical.service.DoctorService;
import com.lumina.backend.infrastructure.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorController.class)
@AutoConfigureMockMvc(addFilters = false)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DoctorService doctorService;

    @MockitoBean
    private AppointmentService appointmentService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    @DisplayName("GET /doctors should return list of doctors")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllDoctors_ShouldReturnList() throws Exception {
        DoctorResponse doc1 = DoctorResponse.builder()
                .id(1L)
                .fullName("Dr. Strange")
                .specialty("Neurology")
                .branchName("Central Hospital")
                .build();

        given(doctorService.getAllDoctors(null)).willReturn(List.of(doc1));

        mockMvc.perform(get("/doctors")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Dr. Strange"))
                .andExpect(jsonPath("$[0].specialty").value("Neurology"));
    }
}