package com.sabormayor.payment.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sabormayor.common.error.ApiException;
import com.sabormayor.common.security.Roles;
import com.sabormayor.payment.application.PaymentService;
import com.sabormayor.payment.domain.Payment;
import com.sabormayor.payment.domain.PaymentStatus;
import com.sabormayor.payment.mapper.PaymentMapper;
import com.sabormayor.payment.web.dto.CreatePaymentRequest;
import com.sabormayor.payment.web.dto.PaymentResponse;
import com.sabormayor.payment.web.dto.RefundRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper mapper;

    @PostMapping
    @Operation(summary = "Charge an order (amount + optional tip) through the configured gateway")
    public ResponseEntity<PaymentResponse> charge(@AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreatePaymentRequest request) {
        Payment payment = paymentService.charge(UUID.fromString(jwt.getSubject()), request);
        if (payment.getStatus() == PaymentStatus.FAILED) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY, "Payment was declined by the gateway");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(payment));
    }

    @PostMapping("/{paymentId}/refund")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Partial or total refund (admin)")
    public PaymentResponse refund(@PathVariable UUID paymentId, @Valid @RequestBody RefundRequest request) {
        paymentService.refund(paymentId, request);
        return mapper.toResponse(paymentService.get(paymentId, null, true));
    }

    @GetMapping("/me")
    public List<PaymentResponse> myPayments(@AuthenticationPrincipal Jwt jwt) {
        return mapper.toResponses(paymentService.myPayments(UUID.fromString(jwt.getSubject())));
    }

    @GetMapping("/{paymentId}")
    public PaymentResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID paymentId) {
        String role = jwt.getClaimAsString("role");
        boolean isStaff = role != null && !Roles.CLIENTE.equals(role);
        return mapper.toResponse(paymentService.get(paymentId, UUID.fromString(jwt.getSubject()), isStaff));
    }

    @GetMapping("/by-order/{orderId}")
    @PreAuthorize("hasAnyRole('MESERO','ADMIN','SUPER_ADMIN')")
    public List<PaymentResponse> byOrder(@PathVariable UUID orderId) {
        return mapper.toResponses(paymentService.byOrder(orderId));
    }
}
