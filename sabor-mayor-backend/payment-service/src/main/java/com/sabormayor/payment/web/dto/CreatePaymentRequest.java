package com.sabormayor.payment.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.sabormayor.payment.domain.PaymentMethod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePaymentRequest(
        @NotNull UUID orderId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @DecimalMin("0.00") BigDecimal tip,
        @NotNull PaymentMethod method,
        @Size(max = 255) String paymentMethodToken) {
}
