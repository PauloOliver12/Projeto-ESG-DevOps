package br.com.projetof.esg.service;

import br.com.projetof.esg.domain.AuditStatus;
import br.com.projetof.esg.domain.LicenseStatus;
import br.com.projetof.esg.dto.DashboardSummaryResponse;
import br.com.projetof.esg.repository.ComplianceAlertRepository;
import br.com.projetof.esg.repository.EnvironmentalLicenseRepository;
import br.com.projetof.esg.repository.InternalAuditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private EnvironmentalLicenseRepository licenseRepository;

    @Mock
    private ComplianceAlertRepository alertRepository;

    @Mock
    private InternalAuditRepository auditRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void shouldAggregateIndicators() {
        when(licenseRepository.count()).thenReturn(10L);
        when(licenseRepository.countByStatus(LicenseStatus.ACTIVE)).thenReturn(7L);
        when(licenseRepository.countByExpiresAtBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(2L);
        when(alertRepository.countByResolvedFalse()).thenReturn(3L);
        when(auditRepository.countByStatus(AuditStatus.SCHEDULED)).thenReturn(4L);

        DashboardSummaryResponse summary = dashboardService.summary();

        assertThat(summary).isEqualTo(new DashboardSummaryResponse(10L, 7L, 2L, 3L, 4L));
    }
}
