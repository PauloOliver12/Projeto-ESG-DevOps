package br.com.projetof.esg.service;

import br.com.projetof.esg.domain.AlertSeverity;
import br.com.projetof.esg.dto.AlertResponse;
import br.com.projetof.esg.dto.CreateAlertRequest;
import br.com.projetof.esg.entity.ComplianceAlert;
import br.com.projetof.esg.exception.BusinessRuleException;
import br.com.projetof.esg.exception.ResourceNotFoundException;
import br.com.projetof.esg.repository.ComplianceAlertRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    @Mock
    private ComplianceAlertRepository repository;

    @Mock
    private LicenseService licenseService;

    @InjectMocks
    private AlertService alertService;

    @Test
    void shouldCreateAlertLinkedToLicense() {
        when(licenseService.findEntity(1L)).thenReturn(LicenseServiceTest.license(1L, "LIC-001"));
        when(repository.save(any(ComplianceAlert.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AlertResponse response = alertService.create(
                new CreateAlertRequest(1L, AlertSeverity.HIGH, "Efluente acima do limite", false));

        assertThat(response.licenseCode()).isEqualTo("LIC-001");
        assertThat(response.severity()).isEqualTo(AlertSeverity.HIGH);
        assertThat(response.resolved()).isFalse();
    }

    @Test
    void shouldResolveOpenAlert() {
        ComplianceAlert alert = alert(false);
        when(repository.findById(10L)).thenReturn(Optional.of(alert));
        when(repository.save(alert)).thenReturn(alert);

        AlertResponse response = alertService.resolve(10L);

        assertThat(response.resolved()).isTrue();
    }

    @Test
    void shouldRejectResolvingAlreadyResolvedAlert() {
        when(repository.findById(10L)).thenReturn(Optional.of(alert(true)));

        assertThatThrownBy(() -> alertService.resolve(10L))
                .isInstanceOf(BusinessRuleException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenAlertDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertService.resolve(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private ComplianceAlert alert(boolean resolved) {
        ComplianceAlert alert = new ComplianceAlert();
        alert.setId(10L);
        alert.setEnvironmentalLicense(LicenseServiceTest.license(1L, "LIC-001"));
        alert.setSeverity(AlertSeverity.MEDIUM);
        alert.setMessage("Relatorio pendente");
        alert.setResolved(resolved);
        alert.setCreatedAt(LocalDateTime.now());
        return alert;
    }
}
