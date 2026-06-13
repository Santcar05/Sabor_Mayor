package com.sabormayor.user.infrastructure;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sabormayor.user.domain.Address;

public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByCustomerId(UUID customerId);

    Optional<Address> findByIdAndCustomerId(UUID id, UUID customerId);

    void deleteByCustomerId(UUID customerId);
}
