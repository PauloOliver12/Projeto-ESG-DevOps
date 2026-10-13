package br.com.projetof.esg.service;

import br.com.projetof.esg.domain.LicenseStatus;
import br.com.projetof.esg.dto.CreateLicenseRequest;
import br.com.projetof.esg.dto.LicenseResponse;
import br.com.projetof.esg.dto.UpdateLicenseRequest;
import br.com.projetof.esg.entity.EnvironmentalLicense;
import br.com.projetof.esg.exception.BusinessRuleException;
import br.com.projetof.esg.exception.ResourceNotFoundException;
import br.com.projetof.esg.repository.EnvironmentalLicenseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LicenseServiceTest {

    @Mock
    private EnvironmentalLicenseRepository repository;

    @InjectMocks
    private LicenseService licenseService;

    @Test
    void shouldCreateLicenseWhenDataIsValid() {
        CreateLicenseRequest request = createRequest("LIC-001", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        when(repository.existsByCode("LIC-001")).thenReturn(false);
        when(repository.save(any(EnvironmentalLicense.class))).thenAnswer(invocation -> {
            EnvironmentalLicense saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        LicenseResponse response = licenseService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.code()).isEqualTo("LIC-001");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
    }

    @Test
    void shouldRejectLicenseWhenExpirationIsNotAfterIssueDate() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        CreateLicenseRequest request = createRequest("LIC-002", date, date);

        assertThatThrownBy(() -> licenseService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("expiracao");
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectLicenseWhenCodeAlreadyExists() {
        CreateLicenseRequest request = createRequest("LIC-003", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1));
        when(repository.existsByCode("LIC-003")).thenReturn(true);

        assertThatThrownBy(() -> licenseService.create(request))
                .isInstanceOf(BusinessRuleException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectUpdateWhenCodeBelongsToAnotherLicense() {
        EnvironmentalLicense existing = license(1L, "LIC-001");
        EnvironmentalLicense other = license(2L, "LIC-999");
        UpdateLicenseRequest request = new UpdateLicenseRequest("LIC-999", "Empresa", "Operacao",
                LicenseStatus.ACTIVE, LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1), "esg@empresa.com");
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.findByCode("LIC-999")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> licenseService.update(1L, request))
                .isInstanceOf(BusinessRuleException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenLicenseDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> licenseService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldFilterLicensesByStatus() {
        when(repository.findByStatus(LicenseStatus.ACTIVE)).thenReturn(List.of(license(1L, "LIC-001")));

        List<LicenseResponse> result = licenseService.findAll(LicenseStatus.ACTIVE);

        assertThat(result).hasSize(1);
        verify(repository, never()).findAll();
    }

    private CreateLicenseRequest createRequest(String code, LocalDate issuedAt, LocalDate expiresAt) {
        return new CreateLicenseRequest(code, "Empresa Verde", "Licenca de Operacao",
                LicenseStatus.ACTIVE, issuedAt, expiresAt, "esg@empresa.com");
    }

    static EnvironmentalLicense license(Long id, String code) {
        EnvironmentalLicense license = new EnvironmentalLicense();
        license.setId(id);
        license.setCode(code);
        license.setCompanyName("Empresa Verde");
        license.setLicenseType("Licenca de Operacao");
        license.setStatus(LicenseStatus.ACTIVE);
        license.setIssuedAt(LocalDate.of(2026, 1, 1));
        license.setExpiresAt(LocalDate.of(2027, 1, 1));
        license.setResponsibleEmail("esg@empresa.com");
        return license;
    }
}
