package com.sabormayor.order.infrastructure;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.order.domain.RestaurantTable;

public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, UUID> {

    Optional<RestaurantTable> findByQrToken(String qrToken);

    Optional<RestaurantTable> findByNumber(int number);

    boolean existsByNumber(int number);
}
