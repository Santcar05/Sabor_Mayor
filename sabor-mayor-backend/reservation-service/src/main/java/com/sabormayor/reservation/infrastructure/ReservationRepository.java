package com.sabormayor.reservation.infrastructure;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sabormayor.reservation.domain.Reservation;
import com.sabormayor.reservation.domain.ReservationStatus;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    List<Reservation> findByCustomerIdOrderByDateDesc(UUID customerId);

    List<Reservation> findByDateOrderByTimeAsc(LocalDate date);

    @Query("""
            select coalesce(sum(r.partySize), 0) from Reservation r
            where r.date = :date and r.time = :time and r.status in ('PENDING', 'CONFIRMED')
            """)
    int bookedSeats(LocalDate date, LocalTime time);

    @Query("""
            select r from Reservation r
            where r.status in ('PENDING', 'CONFIRMED')
              and r.reminderSentAt is null
              and r.date = :date
            """)
    List<Reservation> findReminderCandidates(LocalDate date);
}
