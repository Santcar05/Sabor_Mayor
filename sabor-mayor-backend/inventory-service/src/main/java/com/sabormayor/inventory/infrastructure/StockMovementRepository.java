package com.sabormayor.inventory.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.inventory.domain.StockMovement;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    List<StockMovement> findByIngredientIdOrderByCreatedAtDesc(UUID ingredientId);

    boolean existsByOrderId(UUID orderId);
}
