package br.com.projetof.esg.dto;

import br.com.projetof.esg.domain.AuditStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CreateAuditRequest(
        @NotNull @Positive Long licenseId,
        @NotBlank @Size(max = 120) String auditorName,
        @NotNull LocalDateTime scheduledAt,
        @NotNull AuditStatus status,
        @Size(max = 2000) String findings
) {
}
