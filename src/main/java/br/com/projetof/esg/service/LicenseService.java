package br.com.projetof.esg.service;

import br.com.projetof.esg.domain.LicenseStatus;
import br.com.projetof.esg.dto.CreateLicenseRequest;
import br.com.projetof.esg.dto.LicenseResponse;
import br.com.projetof.esg.dto.UpdateLicenseRequest;
import br.com.projetof.esg.entity.EnvironmentalLicense;
import br.com.projetof.esg.exception.BusinessRuleException;
import br.com.projetof.esg.exception.ResourceNotFoundException;
import br.com.projetof.esg.repository.EnvironmentalLicenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LicenseService {

    private final EnvironmentalLicenseRepository environmentalLicenseRepository;

    public LicenseService(EnvironmentalLicenseRepository environmentalLicenseRepository) {
        this.environmentalLicenseRepository = environmentalLicenseRepository;
    }

    @Transactional(readOnly = true)
    public List<LicenseResponse> findAll(LicenseStatus status) {
        List<EnvironmentalLicense> licenses = status == null
                ? environmentalLicenseRepository.findAll()
                : environmentalLicenseRepository.findByStatus(status);
        return licenses.stream().map(LicenseResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public LicenseResponse findById(Long id) {
        return LicenseResponse.from(findEntity(id));
    }

    @Transactional
    public LicenseResponse create(CreateLicenseRequest request) {
        validateDates(request.issuedAt(), request.expiresAt());
        if (environmentalLicenseRepository.existsByCode(request.code())) {
            throw new BusinessRuleException("Ja existe uma licenca com o codigo informado.");
        }

        EnvironmentalLicense license = new EnvironmentalLicense();
        map(request, license);
        LocalDateTime now = LocalDateTime.now();
        license.setCreatedAt(now);
        license.setUpdatedAt(now);

        return LicenseResponse.from(environmentalLicenseRepository.save(license));
    }

    @Transactional
    public LicenseResponse update(Long id, UpdateLicenseRequest request) {
        validateDates(request.issuedAt(), request.expiresAt());

        EnvironmentalLicense existing = findEntity(id);
        environmentalLicenseRepository.findByCode(request.code())
                .filter(found -> !found.getId().equals(id))
                .ifPresent(found -> {
                    throw new BusinessRuleException("Ja existe outra licenca com o codigo informado.");
                });

        existing.setCode(request.code());
        existing.setCompanyName(request.companyName());
        existing.setLicenseType(request.licenseType());
        existing.setStatus(request.status());
        existing.setIssuedAt(request.issuedAt());
        existing.setExpiresAt(request.expiresAt());
        existing.setResponsibleEmail(request.responsibleEmail());
        existing.setUpdatedAt(LocalDateTime.now());

        return LicenseResponse.from(environmentalLicenseRepository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        EnvironmentalLicense license = findEntity(id);
        environmentalLicenseRepository.delete(license);
    }

    @Transactional(readOnly = true)
    public EnvironmentalLicense findEntity(Long id) {
        return environmentalLicenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Licenca ambiental nao encontrada."));
    }

    private void validateDates(java.time.LocalDate issuedAt, java.time.LocalDate expiresAt) {
        if (!expiresAt.isAfter(issuedAt)) {
            throw new BusinessRuleException("A data de expiracao deve ser posterior a data de emissao.");
        }
    }

    private void map(CreateLicenseRequest request, EnvironmentalLicense license) {
        license.setCode(request.code());
        license.setCompanyName(request.companyName());
        license.setLicenseType(request.licenseType());
        license.setStatus(request.status());
        license.setIssuedAt(request.issuedAt());
        license.setExpiresAt(request.expiresAt());
        license.setResponsibleEmail(request.responsibleEmail());
    }
}
