package br.com.projetof.esg.controller;

import br.com.projetof.esg.config.JsonAccessDeniedHandler;
import br.com.projetof.esg.config.JsonAuthenticationEntryPoint;
import br.com.projetof.esg.config.SecurityConfig;
import br.com.projetof.esg.domain.LicenseStatus;
import br.com.projetof.esg.dto.DashboardSummaryResponse;
import br.com.projetof.esg.dto.LicenseResponse;
import br.com.projetof.esg.exception.ResourceNotFoundException;
import br.com.projetof.esg.service.DashboardService;
import br.com.projetof.esg.service.LicenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {LicenseController.class, DashboardController.class})
@Import({SecurityConfig.class, JsonAuthenticationEntryPoint.class, JsonAccessDeniedHandler.class})
class LicenseControllerSecurityTest {

    private static final String VALID_LICENSE = """
            {
              "code": "LIC-100",
              "companyName": "Empresa Verde",
              "licenseType": "Licenca de Operacao",
              "status": "ACTIVE",
              "issuedAt": "2026-01-01",
              "expiresAt": "2027-01-01",
              "responsibleEmail": "esg@empresa.com"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LicenseService licenseService;

    @MockBean
    private DashboardService dashboardService;

    @Test
    void shouldReturn401WithoutCredentials() throws Exception {
        mockMvc.perform(get("/api/licenses"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void shouldReturn401WithWrongPassword() throws Exception {
        mockMvc.perform(get("/api/licenses").with(httpBasic("admin", "senha-errada")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void analystCanListLicenses() throws Exception {
        when(licenseService.findAll(null)).thenReturn(List.of(sampleResponse()));

        mockMvc.perform(get("/api/licenses").with(httpBasic("analyst", "Analyst@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("LIC-100"));
    }

    @Test
    void analystCannotCreateLicense() throws Exception {
        mockMvc.perform(post("/api/licenses")
                        .with(httpBasic("analyst", "Analyst@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_LICENSE))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateLicense() throws Exception {
        when(licenseService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/licenses")
                        .with(httpBasic("admin", "Admin@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_LICENSE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldReturn400ForInvalidPayload() throws Exception {
        mockMvc.perform(post("/api/licenses")
                        .with(httpBasic("admin", "Admin@123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\": \"\", \"responsibleEmail\": \"invalido\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validations.code").exists())
                .andExpect(jsonPath("$.validations.responsibleEmail").exists());
    }

    @Test
    void shouldReturn404WhenLicenseNotFound() throws Exception {
        when(licenseService.findById(99L)).thenThrow(new ResourceNotFoundException("Licenca ambiental nao encontrada."));

        mockMvc.perform(get("/api/licenses/99").with(httpBasic("auditor", "Auditor@123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Licenca ambiental nao encontrada."));
    }

    @Test
    void dashboardReturnsSummary() throws Exception {
        when(dashboardService.summary()).thenReturn(new DashboardSummaryResponse(10, 7, 2, 3, 4));

        mockMvc.perform(get("/api/dashboard/summary").with(httpBasic("analyst", "Analyst@123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLicenses").value(10))
                .andExpect(jsonPath("$.openAlerts").value(3));
    }

    private LicenseResponse sampleResponse() {
        LocalDateTime now = LocalDateTime.now();
        return new LicenseResponse(1L, "LIC-100", "Empresa Verde", "Licenca de Operacao",
                LicenseStatus.ACTIVE, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1),
                "esg@empresa.com", now, now);
    }
}
