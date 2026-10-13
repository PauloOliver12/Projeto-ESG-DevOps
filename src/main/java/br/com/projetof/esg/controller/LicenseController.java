package br.com.projetof.esg.controller;

import br.com.projetof.esg.domain.LicenseStatus;
import br.com.projetof.esg.dto.CreateLicenseRequest;
import br.com.projetof.esg.dto.LicenseResponse;
import br.com.projetof.esg.dto.UpdateLicenseRequest;
import br.com.projetof.esg.service.LicenseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/licenses")
public class LicenseController {

    private final LicenseService licenseService;

    public LicenseController(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'ANALYST')")
    public List<LicenseResponse> findAll(@RequestParam(required = false) LicenseStatus status) {
        return licenseService.findAll(status);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'ANALYST')")
    public LicenseResponse findById(@PathVariable Long id) {
        return licenseService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public LicenseResponse create(@Valid @RequestBody CreateLicenseRequest request) {
        return licenseService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public LicenseResponse update(@PathVariable Long id, @Valid @RequestBody UpdateLicenseRequest request) {
        return licenseService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        licenseService.delete(id);
    }
}
