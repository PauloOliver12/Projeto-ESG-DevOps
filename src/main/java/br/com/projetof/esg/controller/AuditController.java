package br.com.projetof.esg.controller;

import br.com.projetof.esg.domain.AuditStatus;
import br.com.projetof.esg.dto.AuditResponse;
import br.com.projetof.esg.dto.CreateAuditRequest;
import br.com.projetof.esg.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audits")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'ANALYST')")
    public List<AuditResponse> findAll(@RequestParam(required = false) AuditStatus status) {
        return auditService.findAll(status);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR')")
    public AuditResponse create(@Valid @RequestBody CreateAuditRequest request) {
        return auditService.create(request);
    }
}
