package com.sabormayor.user.domain;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** The id equals the auth-service user id. */
@Entity
@Table(name = "staff_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffMember {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false, length = 30)
    private String role;

    @Column(length = 100)
    private String position;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "hired_at", nullable = false, updatable = false)
    private Instant hiredAt;

    @PrePersist
    void prePersist() {
        if (hiredAt == null) {
            hiredAt = Instant.now();
        }
    }
}
