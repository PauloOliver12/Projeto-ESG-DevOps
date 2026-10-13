package br.com.projetof.esg.service;

import br.com.projetof.esg.domain.AuditStatus;
import br.com.projetof.esg.dto.AuditResponse;
import br.com.projetof.esg.dto.CreateAuditRequest;
import br.com.projetof.esg.entity.InternalAudit;
import br.com.projetof.esg.exception.BusinessRuleException;
import br.com.projetof.esg.repository.InternalAuditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private InternalAuditRepository repository;

    @Mock
    private LicenseService licenseService;

    @InjectMocks
    private AuditService auditService;

    @Test
    void shouldScheduleAuditInTheFuture() {
        LocalDateTime scheduledAt = LocalDateTime.now().plusDays(7);
        when(licenseService.findEntity(1L)).thenReturn(LicenseServiceTest.license(1L, "LIC-001"));
        when(repository.save(any(InternalAudit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuditResponse response = auditService.create(
                new CreateAuditRequest(1L, "Maria Auditora", scheduledAt, AuditStatus.SCHEDULED, null));

        assertThat(response.licenseCode()).isEqualTo("LIC-001");
        assertThat(response.scheduledAt()).isEqualTo(scheduledAt);
        assertThat(response.status()).isEqualTo(AuditStatus.SCHEDULED);
    }

    @Test
    void shouldRejectAuditScheduledInThePast() {
        CreateAuditRequest request = new CreateAuditRequest(1L, "Maria Auditora",
                LocalDateTime.now().minusDays(1), AuditStatus.SCHEDULED, null);

        assertThatThrownBy(() -> auditService.create(request))
                .isInstanceOf(BusinessRuleException.class);
        verifyNoInteractions(licenseService);
        verify(repository, never()).save(any());
    }
}
