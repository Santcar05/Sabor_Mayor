package com.sabormayor.auth.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.auth.domain.AuditLogEntry;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, UUID> {
}
