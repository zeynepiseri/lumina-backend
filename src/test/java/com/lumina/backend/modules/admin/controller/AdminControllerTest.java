package com.lumina.backend.modules.admin.controller;

import com.lumina.backend.infrastructure.security.JwtService;
import com.lumina.backend.modules.admin.dto.AdminDashboardResponse;
import com.lumina.backend.modules.admin.service.StatisticsService;
import com.lumina.backend.modules.auth.repository.UserRepository;
import com.lumina.backend.modules.auth.service.AuthenticationService;
import com.lumina.backend.modules.operations.dto.BranchDensityResponse;
import com.lumina.backend.shared.audit.AuditLogService;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc
@Import(AdminControllerTest.TestSecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean private StatisticsService statisticsService;
    @MockitoBean private AuthenticationService authenticationService;
    @MockitoBean private UserRepository userRepository;
    @MockitoBean private AuditLogService auditLogService;
    @MockitoBean private JwtService jwtService;  

    @Test
    @DisplayName("GET /admin/dashboard-stats - Should return stats (Authorized)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getDashboardStats_ShouldReturnOk() throws Exception {
        AdminDashboardResponse response = AdminDashboardResponse.builder()
                .totalPatients(100L)
                .monthlyEarnings(5000.0)
                .build();

        given(statisticsService.getDashboardStats()).willReturn(response);

        mockMvc.perform(get("/admin/dashboard-stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPatients").value(100))
                .andExpect(jsonPath("$.monthlyEarnings").value(5000.0));
    }

    @Test
    @DisplayName("GET /admin/polyclinic-density - Should allow ADMIN role")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getPolyclinicDensity_ShouldAllowAdmin() throws Exception {
        BranchDensityResponse density = BranchDensityResponse.builder()
                .branchName("Cardio")
                .occupancyRate(50.0)
                .build();

        given(statisticsService.getPolyclinicDensity()).willReturn(List.of(density));

        mockMvc.perform(get("/admin/polyclinic-density"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].branchName").value("Cardio"));
    }

    @Test
    @DisplayName("GET /admin/polyclinic-density - Should allow REGISTRAR role")
    @WithMockUser(username = "registrar", roles = {"REGISTRAR"})
    void getPolyclinicDensity_ShouldAllowRegistrar() throws Exception {
        given(statisticsService.getPolyclinicDensity()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/polyclinic-density"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin/polyclinic-density - Should forbid PATIENT role")
    @WithMockUser(username = "patient", roles = {"PATIENT"})
    void getPolyclinicDensity_ShouldForbidPatient() throws Exception {
        mockMvc.perform(get("/admin/polyclinic-density"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /admin/audit-logs - Should forbid non-ADMIN users")
    @WithMockUser(username = "registrar", roles = {"REGISTRAR"})
    void getAuditLogs_ShouldForbidNonAdmin() throws Exception {
        mockMvc.perform(get("/admin/audit-logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /admin/audit-logs - Should allow ADMIN users")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAuditLogs_ShouldAllowAdmin() throws Exception {
        given(auditLogService.getRecentLogs()).willReturn(Collections.emptyList());

        mockMvc.perform(get("/admin/audit-logs"))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
    }
}