package br.com.projetof.esg.repository;

import br.com.projetof.esg.entity.ComplianceAlert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplianceAlertRepository extends JpaRepository<ComplianceAlert, Long> {

    List<ComplianceAlert> findByResolved(boolean resolved);

    long countByResolvedFalse();
}
