package br.com.projetof.esg.controller;

import br.com.projetof.esg.dto.AlertResponse;
import br.com.projetof.esg.dto.CreateAlertRequest;
import br.com.projetof.esg.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'ANALYST')")
    public List<AlertResponse> findAll(@RequestParam(required = false) Boolean resolved) {
        return alertService.findAll(resolved);
    }

    @GetMapping("/open")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'ANALYST')")
    public List<AlertResponse> findOpenAlerts() {
        return alertService.findOpenAlerts();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public AlertResponse create(@Valid @RequestBody CreateAlertRequest request) {
        return alertService.create(request);
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public AlertResponse resolve(@PathVariable Long id) {
        return alertService.resolve(id);
    }
}

