package com.sabormayor.notification.application;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sabormayor.notification.application.senders.NotificationSender;
import com.sabormayor.notification.application.senders.Senders;
import com.sabormayor.notification.domain.Notification;
import com.sabormayor.notification.infrastructure.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final Senders.EmailSender emailSender;
    private final Senders.SmsSender smsSender;
    private final Senders.PushSender pushSender;
    private final Senders.WhatsAppSender whatsAppSender;
    private final NotificationRepository notificationRepository;

    @Transactional
    public Notification send(Notification.Channel channel, UUID userId, String recipient,
            String subject, String body, String template) {
        boolean sent = senderFor(channel).send(recipient, subject, body);
        return notificationRepository.save(Notification.builder()
                .userId(userId)
                .channel(channel)
                .recipient(recipient)
                .subject(subject)
                .body(body)
                .template(template)
                .status(sent ? Notification.Status.SENT : Notification.Status.FAILED)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<Notification> findAll(Pageable pageable) {
        return notificationRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Notification> findByUser(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserId(userId, pageable);
    }

    private NotificationSender senderFor(Notification.Channel channel) {
        return switch (channel) {
            case EMAIL -> emailSender;
            case SMS -> smsSender;
            case PUSH -> pushSender;
            case WHATSAPP -> whatsAppSender;
        };
    }
}
