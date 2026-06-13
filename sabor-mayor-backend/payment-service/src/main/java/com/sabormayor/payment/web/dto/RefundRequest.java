package com.sabormayor.payment.web.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

/** Null amount means full (remaining) refund. */
public record RefundRequest(
        @DecimalMin("0.01") BigDecimal amount,
        @Size(max = 300) String reason) {
}
