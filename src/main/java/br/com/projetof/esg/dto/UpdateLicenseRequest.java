package br.com.projetof.esg.dto;

import br.com.projetof.esg.domain.LicenseStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateLicenseRequest(
        @NotBlank @Size(max = 50) String code,
        @NotBlank @Size(max = 150) String companyName,
        @NotBlank @Size(max = 100) String licenseType,
        @NotNull LicenseStatus status,
        @NotNull LocalDate issuedAt,
        @NotNull LocalDate expiresAt,
        @NotBlank @Email @Size(max = 150) String responsibleEmail
) {
}
