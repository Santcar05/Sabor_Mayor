package com.sabormayor.notification.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.sabormayor.common.events.OrderPaidEvent;
import com.sabormayor.common.events.OrderStatusChangedEvent;
import com.sabormayor.common.events.PaymentRefundedEvent;
import com.sabormayor.common.events.ReservationCancelledEvent;
import com.sabormayor.common.events.ReservationCreatedEvent;
import com.sabormayor.common.events.ReservationReminderDueEvent;
import com.sabormayor.common.events.UserDeletedEvent;
import com.sabormayor.common.events.UserRegisteredEvent;
import com.sabormayor.common.kafka.KafkaTopics;
import com.sabormayor.notification.application.NotificationService;
import com.sabormayor.notification.domain.Notification;

import lombok.RequiredArgsConstructor;

/**
 * Single consumer over every domain topic; each event type maps to a template.
 * Recipient phone numbers are not replicated here, so SMS/WhatsApp/push use
 * the user id as the routing reference resolved by the (mock) provider.
 */
@Component
@KafkaListener(
        topics = {KafkaTopics.USERS_EVENTS, KafkaTopics.ORDERS_EVENTS,
                KafkaTopics.RESERVATIONS_EVENTS, KafkaTopics.PAYMENTS_EVENTS},
        groupId = "notification-service",
        autoStartup = "${app.kafka-listeners-enabled:true}")
@RequiredArgsConstructor
public class DomainEventsConsumer {

    private static final Logger log = LoggerFactory.getLogger(DomainEventsConsumer.class);

    private final NotificationService notificationService;

    @KafkaHandler
    public void onUserRegistered(UserRegisteredEvent event) {
        notificationService.send(Notification.Channel.EMAIL, event.userId(), event.email(),
                "¡Bienvenido a Sabor Mayor!",
                "Hola %s, tu cuenta fue creada. Te esperamos para vivir la experiencia Sabor Mayor."
                        .formatted(event.fullName()),
                "user-welcome");
    }

    @KafkaHandler
    public void onUserDeleted(UserDeletedEvent event) {
        notificationService.send(Notification.Channel.EMAIL, event.userId(), event.email(),
                "Confirmación de eliminación de cuenta",
                "Tu cuenta y tus datos personales fueron eliminados conforme a tu solicitud (Habeas Data).",
                "user-deleted");
    }

    @KafkaHandler
    public void onReservationCreated(ReservationCreatedEvent event) {
        String detail = "Reserva confirmada para el %s a las %s, %d personas."
                .formatted(event.date(), event.time(), event.partySize());
        notificationService.send(Notification.Channel.EMAIL, event.customerId(), event.customerEmail(),
                "Tu reserva en Sabor Mayor", detail, "reservation-created");
        notificationService.send(Notification.Channel.WHATSAPP, event.customerId(),
                event.customerId().toString(), null, detail, "reservation-created");
    }

    @KafkaHandler
    public void onReservationReminder(ReservationReminderDueEvent event) {
        notificationService.send(Notification.Channel.EMAIL, event.customerId(), event.customerEmail(),
                "Recordatorio: tu reserva es mañana",
                "Te esperamos mañana %s a las %s. Mesa para %d."
                        .formatted(event.date(), event.time(), event.partySize()),
                "reservation-reminder");
    }

    @KafkaHandler
    public void onReservationCancelled(ReservationCancelledEvent event) {
        notificationService.send(Notification.Channel.EMAIL, event.customerId(), event.customerEmail(),
                "Reserva cancelada",
                "Tu reserva del %s a las %s fue cancelada.".formatted(event.date(), event.time()),
                "reservation-cancelled");
    }

    @KafkaHandler
    public void onOrderStatusChanged(OrderStatusChangedEvent event) {
        notificationService.send(Notification.Channel.PUSH, event.customerId(),
                event.customerId().toString(),
                "Actualización de tu pedido",
                "Tu pedido pasó de %s a %s.".formatted(event.previousStatus(), event.newStatus()),
                "order-status-changed");
    }

    @KafkaHandler
    public void onOrderPaid(OrderPaidEvent event) {
        notificationService.send(Notification.Channel.EMAIL, event.customerId(),
                event.customerId().toString(),
                "Recibo de tu pedido",
                "Pago recibido por $%s. ¡Gracias por visitarnos!".formatted(event.total()),
                "order-receipt");
    }

    @KafkaHandler
    public void onPaymentRefunded(PaymentRefundedEvent event) {
        notificationService.send(Notification.Channel.EMAIL, event.customerId(),
                event.customerId().toString(),
                "Reembolso procesado",
                "Procesamos un reembolso de $%s%s.".formatted(
                        event.amount(), event.partial() ? " (parcial)" : ""),
                "payment-refunded");
    }

    @KafkaHandler(isDefault = true)
    public void onUnknown(Object event) {
        log.debug("Ignoring event type {}", event.getClass().getSimpleName());
    }
}
