package com.sabormayor.menu.infrastructure;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.menu.domain.DishPriceHistory;

public interface DishPriceHistoryRepository extends JpaRepository<DishPriceHistory, UUID> {

    List<DishPriceHistory> findByDishIdOrderByChangedAtDesc(UUID dishId);
}
