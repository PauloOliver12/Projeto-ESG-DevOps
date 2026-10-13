package br.com.projetof.esg.dto;

import br.com.projetof.esg.domain.AuditStatus;
import br.com.projetof.esg.entity.InternalAudit;

import java.time.LocalDateTime;

public record AuditResponse(
        Long id,
        Long licenseId,
        String licenseCode,
        String auditorName,
        LocalDateTime scheduledAt,
        AuditStatus status,
        String findings,
        LocalDateTime createdAt
) {
    public static AuditResponse from(InternalAudit audit) {
        return new AuditResponse(
                audit.getId(),
                audit.getEnvironmentalLicense().getId(),
                audit.getEnvironmentalLicense().getCode(),
                audit.getAuditorName(),
                audit.getScheduledAt(),
                audit.getStatus(),
                audit.getFindings(),
                audit.getCreatedAt()
        );
    }
}
