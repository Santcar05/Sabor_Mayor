package com.sabormayor.payment.infrastructure;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.sabormayor.payment.application.PaymentGateway;

/**
 * Dev adapter: approves everything except the magic token "tok_fail",
 * which simulates a declined card.
 */
@Component
@Profile("!prod")
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public String name() {
        return "MOCK";
    }

    @Override
    public ChargeResult charge(ChargeRequest request) {
        if ("tok_fail".equals(request.paymentMethodToken())) {
            return new ChargeResult(false, null, "Card declined (mock)");
        }
        return new ChargeResult(true, "mock-ch-" + UUID.randomUUID(), null);
    }

    @Override
    public RefundResult refund(String gatewayReference, BigDecimal amount) {
        return new RefundResult(true, "mock-re-" + UUID.randomUUID(), null);
    }
}
