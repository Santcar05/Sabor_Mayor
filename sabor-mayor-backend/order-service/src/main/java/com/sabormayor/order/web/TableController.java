package com.sabormayor.order.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sabormayor.order.application.TableService;
import com.sabormayor.order.mapper.OrderMapper;
import com.sabormayor.order.web.dto.OrderDtos;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
@Tag(name = "Tables")
public class TableController {

    private final TableService tableService;
    private final OrderMapper mapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('MESERO','COCINERO','ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Board with every table and its status (waiter app)")
    public List<OrderDtos.TableResponse> tables() {
        return mapper.toTableResponses(tableService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<OrderDtos.TableResponse> create(@Valid @RequestBody OrderDtos.CreateTable request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(tableService.create(request.number(), request.capacity())));
    }

    @PatchMapping("/{tableId}/status")
    @PreAuthorize("hasAnyRole('MESERO','ADMIN','SUPER_ADMIN')")
    public OrderDtos.TableResponse updateStatus(@PathVariable UUID tableId,
            @Valid @RequestBody OrderDtos.UpdateTableStatus request) {
        return mapper.toResponse(tableService.updateStatus(tableId, request.status()));
    }

    @GetMapping("/by-qr/{qrToken}")
    @Operation(summary = "Resolve the table from a scanned QR code (customer)")
    public OrderDtos.TableResponse byQr(@PathVariable String qrToken) {
        return mapper.toResponse(tableService.findByQrToken(qrToken));
    }
}
