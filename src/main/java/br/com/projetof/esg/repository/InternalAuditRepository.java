package br.com.projetof.esg.repository;

import br.com.projetof.esg.domain.AuditStatus;
import br.com.projetof.esg.entity.InternalAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InternalAuditRepository extends JpaRepository<InternalAudit, Long> {

    List<InternalAudit> findByStatus(AuditStatus status);

    long countByStatus(AuditStatus status);
}
