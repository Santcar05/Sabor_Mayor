package com.sabormayor.loyalty.infrastructure;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.loyalty.domain.LoyaltyAccount;

public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, UUID> {
}
