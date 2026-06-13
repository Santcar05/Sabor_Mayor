package com.sabormayor.payment.application;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sabormayor.common.error.ApiException;
import com.sabormayor.common.error.BusinessRuleException;
import com.sabormayor.common.error.ResourceNotFoundException;
import com.sabormayor.common.events.PaymentConfirmedEvent;
import com.sabormayor.common.events.PaymentRefundedEvent;
import com.sabormayor.common.kafka.KafkaTopics;
import com.sabormayor.payment.domain.OutboxEvent;
import com.sabormayor.payment.domain.Payment;
import com.sabormayor.payment.domain.PaymentStatus;
import com.sabormayor.payment.domain.Refund;
import com.sabormayor.payment.infrastructure.OutboxRepository;
import com.sabormayor.payment.infrastructure.PaymentRepository;
import com.sabormayor.payment.infrastructure.RefundRepository;
import com.sabormayor.payment.web.dto.CreatePaymentRequest;
import com.sabormayor.payment.web.dto.RefundRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final OutboxRepository outboxRepository;
    private final PaymentGateway paymentGateway;
    private final ObjectMapper objectMapper;

    @Transactional
    public Payment charge(UUID customerId, CreatePaymentRequest request) {
        BigDecimal tip = request.tip() != null ? request.tip() : BigDecimal.ZERO;
        BigDecimal totalToCharge = request.amount().add(tip);

        PaymentGateway.ChargeResult result = paymentGateway.charge(new PaymentGateway.ChargeRequest(
                request.orderId(), customerId, totalToCharge, request.paymentMethodToken()));

        Payment payment = Payment.builder()
                .orderId(request.orderId())
                .customerId(customerId)
                .amount(request.amount())
                .tip(tip)
                .method(request.method())
                .gateway(paymentGateway.name())
                .gatewayReference(result.gatewayReference())
                .status(result.success() ? PaymentStatus.CONFIRMED : PaymentStatus.FAILED)
                .build();
        payment = paymentRepository.save(payment);

        // The FAILED attempt is persisted (audit); the controller turns it into a 422.
        if (!result.success()) {
            return payment;
        }

        appendOutbox(payment.getId(), new PaymentConfirmedEvent(
                payment.getId(), payment.getOrderId(), payment.getCustomerId(),
                payment.getAmount(), payment.getTip(), payment.getMethod().name(), Instant.now()));
        return payment;
    }

    @Transactional
    public Refund refund(UUID paymentId, RefundRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment", paymentId));
        if (payment.getStatus() != PaymentStatus.CONFIRMED
                && payment.getStatus() != PaymentStatus.PARTIALLY_REFUNDED) {
            throw new BusinessRuleException("Payment cannot be refunded in status " + payment.getStatus());
        }
        BigDecimal refundable = payment.totalCharged().subtract(payment.getRefundedAmount());
        BigDecimal amount = request.amount() != null ? request.amount() : refundable;
        if (amount.signum() <= 0 || amount.compareTo(refundable) > 0) {
            throw new BusinessRuleException(
                    "Refund amount must be between 0 and %s".formatted(refundable));
        }

        PaymentGateway.RefundResult result = paymentGateway.refund(payment.getGatewayReference(), amount);
        if (!result.success()) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Refund was rejected: " + result.failureReason());
        }

        Refund refund = refundRepository.save(Refund.builder()
                .paymentId(payment.getId())
                .amount(amount)
                .reason(request.reason())
                .gatewayReference(result.gatewayReference())
                .build());

        payment.setRefundedAmount(payment.getRefundedAmount().add(amount));
        boolean partial = payment.getRefundedAmount().compareTo(payment.totalCharged()) < 0;
        payment.setStatus(partial ? PaymentStatus.PARTIALLY_REFUNDED : PaymentStatus.REFUNDED);

        appendOutbox(payment.getId(), new PaymentRefundedEvent(
                payment.getId(), payment.getOrderId(), payment.getCustomerId(),
                amount, partial, Instant.now()));
        return refund;
    }

    @Transactional(readOnly = true)
    public List<Payment> myPayments(UUID customerId) {
        return paymentRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    @Transactional(readOnly = true)
    public Payment get(UUID paymentId, UUID requesterId, boolean isStaff) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment", paymentId));
        if (!isStaff && !payment.getCustomerId().equals(requesterId)) {
            throw ResourceNotFoundException.of("Payment", paymentId);
        }
        return payment;
    }

    @Transactional(readOnly = true)
    public List<Payment> byOrder(UUID orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    private void appendOutbox(UUID aggregateId, Object event) {
        try {
            outboxRepository.save(OutboxEvent.builder()
                    .aggregateType("Payment")
                    .aggregateId(aggregateId)
                    .eventType(event.getClass().getSimpleName())
                    .payload(objectMapper.writeValueAsString(event))
                    .topic(KafkaTopics.PAYMENTS_EVENTS)
                    .build());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize outbox event", e);
        }
    }
}
