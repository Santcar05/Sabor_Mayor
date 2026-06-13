package com.sabormayor.reservation.infrastructure;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.reservation.domain.BlockedDate;

public interface BlockedDateRepository extends JpaRepository<BlockedDate, UUID> {

    boolean existsByDate(LocalDate date);
}
