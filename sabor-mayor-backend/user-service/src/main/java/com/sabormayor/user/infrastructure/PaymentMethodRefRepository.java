package com.sabormayor.user.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.user.domain.PaymentMethodRef;

public interface PaymentMethodRefRepository extends JpaRepository<PaymentMethodRef, UUID> {

    List<PaymentMethodRef> findByCustomerId(UUID customerId);

    Optional<PaymentMethodRef> findByIdAndCustomerId(UUID id, UUID customerId);

    void deleteByCustomerId(UUID customerId);
}
