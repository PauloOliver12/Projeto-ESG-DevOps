package br.com.projetof.esg.service;

import br.com.projetof.esg.domain.AuditStatus;
import br.com.projetof.esg.dto.AuditResponse;
import br.com.projetof.esg.dto.CreateAuditRequest;
import br.com.projetof.esg.entity.EnvironmentalLicense;
import br.com.projetof.esg.entity.InternalAudit;
import br.com.projetof.esg.exception.BusinessRuleException;
import br.com.projetof.esg.repository.InternalAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditService {

    private final InternalAuditRepository internalAuditRepository;
    private final LicenseService licenseService;

    public AuditService(InternalAuditRepository internalAuditRepository, LicenseService licenseService) {
        this.internalAuditRepository = internalAuditRepository;
        this.licenseService = licenseService;
    }

    @Transactional(readOnly = true)
    public List<AuditResponse> findAll(AuditStatus status) {
        List<InternalAudit> audits = status == null
                ? internalAuditRepository.findAll()
                : internalAuditRepository.findByStatus(status);
        return audits.stream().map(AuditResponse::from).toList();
    }

    @Transactional
    public AuditResponse create(CreateAuditRequest request) {
        if (request.scheduledAt().isBefore(LocalDateTime.now())) {
            throw new BusinessRuleException("A auditoria deve ser agendada para o presente ou futuro.");
        }

        EnvironmentalLicense license = licenseService.findEntity(request.licenseId());

        InternalAudit audit = new InternalAudit();
        audit.setEnvironmentalLicense(license);
        audit.setAuditorName(request.auditorName());
        audit.setScheduledAt(request.scheduledAt());
        audit.setStatus(request.status());
        audit.setFindings(request.findings());
        audit.setCreatedAt(LocalDateTime.now());

        return AuditResponse.from(internalAuditRepository.save(audit));
    }
}
