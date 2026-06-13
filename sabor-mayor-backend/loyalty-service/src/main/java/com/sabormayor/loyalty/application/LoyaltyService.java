package com.sabormayor.loyalty.application;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sabormayor.common.error.BusinessRuleException;
import com.sabormayor.common.events.OrderPaidEvent;
import com.sabormayor.loyalty.domain.LoyaltyAccount;
import com.sabormayor.loyalty.domain.LoyaltyLevel;
import com.sabormayor.loyalty.domain.LoyaltyTransaction;
import com.sabormayor.loyalty.infrastructure.LoyaltyAccountRepository;
import com.sabormayor.loyalty.infrastructure.LoyaltyTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    /** 1 Sabor Point per 1.000 COP spent. */
    private static final BigDecimal POINTS_DIVISOR = new BigDecimal("1000");

    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyTransactionRepository transactionRepository;

    @Transactional
    public LoyaltyAccount getOrCreateAccount(UUID userId) {
        return accountRepository.findById(userId)
                .orElseGet(() -> accountRepository.save(LoyaltyAccount.builder()
                        .userId(userId)
                        .points(0)
                        .lifetimePoints(0)
                        .level(LoyaltyLevel.ALUMNO_CULINARIO)
                        .build()));
    }

    /** Consumes OrderPaid: idempotent per order. */
    @Transactional
    public void earnFromOrder(OrderPaidEvent event) {
        if (transactionRepository.existsByOrderIdAndType(event.orderId(), LoyaltyTransaction.Type.EARN)) {
            return;
        }
        int earned = event.total().divide(POINTS_DIVISOR, 0, java.math.RoundingMode.FLOOR).intValue();
        if (earned <= 0) {
            return;
        }
        LoyaltyAccount account = getOrCreateAccount(event.customerId());
        account.earn(earned);
        transactionRepository.save(LoyaltyTransaction.builder()
                .userId(event.customerId())
                .points(earned)
                .type(LoyaltyTransaction.Type.EARN)
                .orderId(event.orderId())
                .description("Puntos por pedido")
                .build());
    }

    @Transactional
    public LoyaltyAccount redeem(UUID userId, int points, String description) {
        if (points <= 0) {
            throw new BusinessRuleException("Points to redeem must be positive");
        }
        LoyaltyAccount account = getOrCreateAccount(userId);
        if (account.getPoints() < points) {
            throw new BusinessRuleException(
                    "Insufficient points: balance is %d".formatted(account.getPoints()));
        }
        account.redeem(points);
        transactionRepository.save(LoyaltyTransaction.builder()
                .userId(userId)
                .points(-points)
                .type(LoyaltyTransaction.Type.REDEEM)
                .description(description != null ? description : "Canje de puntos")
                .build());
        return account;
    }

    @Transactional(readOnly = true)
    public List<LoyaltyTransaction> transactions(UUID userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}
