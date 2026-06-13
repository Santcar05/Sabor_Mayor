package com.sabormayor.loyalty.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.loyalty.domain.LoyaltyTransaction;

public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, UUID> {

    List<LoyaltyTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId);

    boolean existsByOrderIdAndType(UUID orderId, LoyaltyTransaction.Type type);
}
