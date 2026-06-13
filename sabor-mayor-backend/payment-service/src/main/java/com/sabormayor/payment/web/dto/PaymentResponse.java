package com.sabormayor.payment.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.sabormayor.payment.domain.PaymentMethod;
import com.sabormayor.payment.domain.PaymentStatus;

public record PaymentResponse(
        UUID id,
        UUID orderId,
        UUID customerId,
        BigDecimal amount,
        BigDecimal tip,
        PaymentMethod method,
        String gateway,
        PaymentStatus status,
        BigDecimal refundedAmount,
        Instant createdAt) {
}
