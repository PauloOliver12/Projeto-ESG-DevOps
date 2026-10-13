package br.com.projetof.esg.dto;

import br.com.projetof.esg.domain.AlertSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateAlertRequest(
        @NotNull @Positive Long licenseId,
        @NotNull AlertSeverity severity,
        @NotBlank @Size(max = 500) String message,
        boolean resolved
) {
}
