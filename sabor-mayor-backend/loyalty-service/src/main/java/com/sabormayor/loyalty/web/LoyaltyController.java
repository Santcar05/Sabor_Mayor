package com.sabormayor.loyalty.web;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sabormayor.loyalty.application.LoyaltyService;
import com.sabormayor.loyalty.domain.LoyaltyAccount;
import com.sabormayor.loyalty.domain.LoyaltyLevel;
import com.sabormayor.loyalty.domain.LoyaltyTransaction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
@Tag(name = "Loyalty — Sabor Points")
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    public record AccountResponse(UUID userId, int points, int lifetimePoints, LoyaltyLevel level,
            LoyaltyLevel nextLevel, Integer pointsToNextLevel) {

        static AccountResponse from(LoyaltyAccount account) {
            LoyaltyLevel next = null;
            Integer missing = null;
            for (LoyaltyLevel level : LoyaltyLevel.values()) {
                if (level.getThreshold() > account.getLifetimePoints()) {
                    next = level;
                    missing = level.getThreshold() - account.getLifetimePoints();
                    break;
                }
            }
            return new AccountResponse(account.getUserId(), account.getPoints(),
                    account.getLifetimePoints(), account.getLevel(), next, missing);
        }
    }

    public record TransactionResponse(UUID id, int points, LoyaltyTransaction.Type type, UUID orderId,
            String description, Instant createdAt) {
    }

    public record RedeemRequest(@Min(1) int points, @Size(max = 255) String description) {
    }

    @GetMapping("/me")
    @Operation(summary = "Balance, level and progress to the next tier")
    public AccountResponse myAccount(@AuthenticationPrincipal Jwt jwt) {
        return AccountResponse.from(loyaltyService.getOrCreateAccount(UUID.fromString(jwt.getSubject())));
    }

    @GetMapping("/me/transactions")
    public List<TransactionResponse> myTransactions(@AuthenticationPrincipal Jwt jwt) {
        return loyaltyService.transactions(UUID.fromString(jwt.getSubject())).stream()
                .map(t -> new TransactionResponse(t.getId(), t.getPoints(), t.getType(), t.getOrderId(),
                        t.getDescription(), t.getCreatedAt()))
                .toList();
    }

    @PostMapping("/me/redeem")
    @Operation(summary = "Redeem points from the balance")
    public AccountResponse redeem(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RedeemRequest request) {
        return AccountResponse.from(loyaltyService.redeem(
                UUID.fromString(jwt.getSubject()), request.points(), request.description()));
    }
}
