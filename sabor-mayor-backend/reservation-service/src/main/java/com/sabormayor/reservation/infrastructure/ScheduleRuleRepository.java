package com.sabormayor.reservation.infrastructure;

import java.time.DayOfWeek;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.reservation.domain.ScheduleRule;

public interface ScheduleRuleRepository extends JpaRepository<ScheduleRule, UUID> {

    Optional<ScheduleRule> findByDayOfWeek(DayOfWeek dayOfWeek);
}
