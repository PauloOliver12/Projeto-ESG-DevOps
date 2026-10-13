package br.com.projetof.esg.service;

import br.com.projetof.esg.dto.AlertResponse;
import br.com.projetof.esg.dto.CreateAlertRequest;
import br.com.projetof.esg.entity.ComplianceAlert;
import br.com.projetof.esg.entity.EnvironmentalLicense;
import br.com.projetof.esg.exception.BusinessRuleException;
import br.com.projetof.esg.exception.ResourceNotFoundException;
import br.com.projetof.esg.repository.ComplianceAlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AlertService {

    private final ComplianceAlertRepository complianceAlertRepository;
    private final LicenseService licenseService;

    public AlertService(ComplianceAlertRepository complianceAlertRepository, LicenseService licenseService) {
        this.complianceAlertRepository = complianceAlertRepository;
        this.licenseService = licenseService;
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> findAll(Boolean resolved) {
        List<ComplianceAlert> alerts = resolved == null
                ? complianceAlertRepository.findAll()
                : complianceAlertRepository.findByResolved(resolved);
        return alerts.stream().map(AlertResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AlertResponse> findOpenAlerts() {
        return complianceAlertRepository.findByResolved(false).stream().map(AlertResponse::from).toList();
    }

    @Transactional
    public AlertResponse create(CreateAlertRequest request) {
        EnvironmentalLicense license = licenseService.findEntity(request.licenseId());

        ComplianceAlert alert = new ComplianceAlert();
        alert.setEnvironmentalLicense(license);
        alert.setSeverity(request.severity());
        alert.setMessage(request.message());
        alert.setResolved(request.resolved());
        alert.setCreatedAt(LocalDateTime.now());

        return AlertResponse.from(complianceAlertRepository.save(alert));
    }

    @Transactional
    public AlertResponse resolve(Long id) {
        ComplianceAlert alert = complianceAlertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta de compliance nao encontrado."));

        if (alert.isResolved()) {
            throw new BusinessRuleException("O alerta informado ja esta resolvido.");
        }

        alert.setResolved(true);
        return AlertResponse.from(complianceAlertRepository.save(alert));
    }
}

