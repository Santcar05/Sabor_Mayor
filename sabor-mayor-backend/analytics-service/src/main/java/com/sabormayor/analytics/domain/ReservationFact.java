package com.sabormayor.analytics.domain;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Reservation lifecycle facts for occupancy and no-show KPIs. */
@Entity
@Table(name = "reservation_facts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationFact {

    @Id
    @Column(name = "reservation_id")
    private UUID reservationId;

    @Column(name = "reservation_date", nullable = false)
    private LocalDate reservationDate;

    @Column(name = "party_size", nullable = false)
    private int partySize;

    @Column(nullable = false)
    private boolean cancelled;
}
