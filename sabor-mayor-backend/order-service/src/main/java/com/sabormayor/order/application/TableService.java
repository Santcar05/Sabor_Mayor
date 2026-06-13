package com.sabormayor.order.application;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sabormayor.common.error.ConflictException;
import com.sabormayor.common.error.ResourceNotFoundException;
import com.sabormayor.order.domain.RestaurantTable;
import com.sabormayor.order.domain.TableStatus;
import com.sabormayor.order.infrastructure.RestaurantTableRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TableService {

    private final RestaurantTableRepository tableRepository;

    @Transactional(readOnly = true)
    public List<RestaurantTable> findAll() {
        return tableRepository.findAll();
    }

    @Transactional
    public RestaurantTable create(int number, int capacity) {
        if (tableRepository.existsByNumber(number)) {
            throw new ConflictException("Table %d already exists".formatted(number));
        }
        return tableRepository.save(RestaurantTable.builder().number(number).capacity(capacity).build());
    }

    @Transactional
    public RestaurantTable updateStatus(UUID tableId, TableStatus status) {
        RestaurantTable table = tableRepository.findById(tableId)
                .orElseThrow(() -> ResourceNotFoundException.of("Table", tableId));
        table.setStatus(status);
        return table;
    }

    @Transactional(readOnly = true)
    public RestaurantTable findByQrToken(String qrToken) {
        return tableRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new ResourceNotFoundException("Unknown table QR code"));
    }
}
