package com.sabormayor.payment.application;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Abstraction over external processors (Stripe, Wompi, Mercado Pago).
 * The dev/default profile uses {@code MockPaymentGateway}; real adapters are
 * activated with the "prod" profile and the corresponding credentials.
 */
public interface PaymentGateway {

    String name();

    ChargeResult charge(ChargeRequest request);

    RefundResult refund(String gatewayReference, BigDecimal amount);

    record ChargeRequest(
            UUID orderId,
            UUID customerId,
            BigDecimal amount,
            String paymentMethodToken) {
    }

    record ChargeResult(boolean success, String gatewayReference, String failureReason) {
    }

    record RefundResult(boolean success, String gatewayReference, String failureReason) {
    }
}
