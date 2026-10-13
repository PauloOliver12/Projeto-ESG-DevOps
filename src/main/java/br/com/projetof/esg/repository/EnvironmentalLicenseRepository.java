package br.com.projetof.esg.repository;

import br.com.projetof.esg.domain.LicenseStatus;
import br.com.projetof.esg.entity.EnvironmentalLicense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EnvironmentalLicenseRepository extends JpaRepository<EnvironmentalLicense, Long> {

    Optional<EnvironmentalLicense> findByCode(String code);

    List<EnvironmentalLicense> findByStatus(LicenseStatus status);

    boolean existsByCode(String code);

    long countByStatus(LicenseStatus status);

    long countByExpiresAtBetween(LocalDate start, LocalDate end);
}
