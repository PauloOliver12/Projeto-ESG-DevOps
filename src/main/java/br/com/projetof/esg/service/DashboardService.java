package br.com.projetof.esg.service;

import br.com.projetof.esg.domain.AuditStatus;
import br.com.projetof.esg.domain.LicenseStatus;
import br.com.projetof.esg.dto.DashboardSummaryResponse;
import br.com.projetof.esg.repository.ComplianceAlertRepository;
import br.com.projetof.esg.repository.EnvironmentalLicenseRepository;
import br.com.projetof.esg.repository.InternalAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class DashboardService {

    private final EnvironmentalLicenseRepository environmentalLicenseRepository;
    private final ComplianceAlertRepository complianceAlertRepository;
    private final InternalAuditRepository internalAuditRepository;

    public DashboardService(EnvironmentalLicenseRepository environmentalLicenseRepository,
                            ComplianceAlertRepository complianceAlertRepository,
                            InternalAuditRepository internalAuditRepository) {
        this.environmentalLicenseRepository = environmentalLicenseRepository;
        this.complianceAlertRepository = complianceAlertRepository;
        this.internalAuditRepository = internalAuditRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary() {
        LocalDate today = LocalDate.now();
        return new DashboardSummaryResponse(
                environmentalLicenseRepository.count(),
                environmentalLicenseRepository.countByStatus(LicenseStatus.ACTIVE),
                environmentalLicenseRepository.countByExpiresAtBetween(today, today.plusDays(30)),
                complianceAlertRepository.countByResolvedFalse(),
                internalAuditRepository.countByStatus(AuditStatus.SCHEDULED)
        );
    }
}
