package br.com.projetof.esg.dto;

import br.com.projetof.esg.domain.LicenseStatus;
import br.com.projetof.esg.entity.EnvironmentalLicense;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LicenseResponse(
        Long id,
        String code,
        String companyName,
        String licenseType,
        LicenseStatus status,
        LocalDate issuedAt,
        LocalDate expiresAt,
        String responsibleEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static LicenseResponse from(EnvironmentalLicense license) {
        return new LicenseResponse(
                license.getId(),
                license.getCode(),
                license.getCompanyName(),
                license.getLicenseType(),
                license.getStatus(),
                license.getIssuedAt(),
                license.getExpiresAt(),
                license.getResponsibleEmail(),
                license.getCreatedAt(),
                license.getUpdatedAt()
        );
    }
}
