package com.sabormayor.analytics.infrastructure;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sabormayor.analytics.domain.ReservationFact;

public interface ReservationFactRepository extends JpaRepository<ReservationFact, UUID> {

    @Query("select count(r) from ReservationFact r where r.reservationDate between :from and :to")
    long total(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select count(r) from ReservationFact r
            where r.reservationDate between :from and :to and r.cancelled = true
            """)
    long cancelled(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("""
            select coalesce(sum(r.partySize), 0) from ReservationFact r
            where r.reservationDate between :from and :to and r.cancelled = false
            """)
    long coveredSeats(@Param("from") LocalDate from, @Param("to") LocalDate to);
}
