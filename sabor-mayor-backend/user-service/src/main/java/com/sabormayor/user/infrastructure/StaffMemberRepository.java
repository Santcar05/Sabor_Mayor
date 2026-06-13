package com.sabormayor.user.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.user.domain.StaffMember;

public interface StaffMemberRepository extends JpaRepository<StaffMember, UUID> {
}
