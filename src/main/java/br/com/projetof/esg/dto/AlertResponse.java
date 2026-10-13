package br.com.projetof.esg.dto;

import br.com.projetof.esg.domain.AlertSeverity;
import br.com.projetof.esg.entity.ComplianceAlert;

import java.time.LocalDateTime;

public record AlertResponse(
        Long id,
        Long licenseId,
        String licenseCode,
        AlertSeverity severity,
        String message,
        boolean resolved,
        LocalDateTime createdAt
) {
    public static AlertResponse from(ComplianceAlert alert) {
        return new AlertResponse(
                alert.getId(),
                alert.getEnvironmentalLicense().getId(),
                alert.getEnvironmentalLicense().getCode(),
                alert.getSeverity(),
                alert.getMessage(),
                alert.isResolved(),
                alert.getCreatedAt()
        );
    }
}
