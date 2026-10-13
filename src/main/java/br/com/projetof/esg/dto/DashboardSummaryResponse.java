package br.com.projetof.esg.dto;

public record DashboardSummaryResponse(
        long totalLicenses,
        long activeLicenses,
        long expiringIn30Days,
        long openAlerts,
        long scheduledAudits
) {
}
