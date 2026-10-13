INSERT INTO environmental_license (
    code,
    company_name,
    license_type,
    status,
    issued_at,
    expires_at,
    responsible_email,
    created_at,
    updated_at
) VALUES (
    'LIC-2026-001',
    'EcoLog Brasil',
    'Operacao de Residuos',
    'ACTIVE',
    DATE '2026-01-10',
    DATE '2027-01-10',
    'compliance@ecolog.com.br',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

INSERT INTO internal_audit (
    environmental_license_id,
    auditor_name,
    scheduled_at,
    status,
    findings,
    created_at
) VALUES (
    1,
    'Marina Duarte',
    TIMESTAMP '2026-06-10 09:00:00',
    'SCHEDULED',
    'Auditoria preventiva para renovacao anual.',
    CURRENT_TIMESTAMP
);

INSERT INTO compliance_alert (
    environmental_license_id,
    severity,
    message,
    resolved,
    created_at
) VALUES (
    1,
    'HIGH',
    'Licenca proxima do vencimento em menos de 30 dias.',
    0,
    CURRENT_TIMESTAMP
);
