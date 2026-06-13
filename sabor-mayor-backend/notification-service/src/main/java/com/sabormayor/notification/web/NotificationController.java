package com.sabormayor.notification.web;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sabormayor.notification.application.NotificationService;
import com.sabormayor.notification.domain.Notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public record NotificationResponse(UUID id, UUID userId, Notification.Channel channel, String recipient,
            String subject, String body, String template, Notification.Status status, Instant createdAt) {

        static NotificationResponse from(Notification n) {
            return new NotificationResponse(n.getId(), n.getUserId(), n.getChannel(), n.getRecipient(),
                    n.getSubject(), n.getBody(), n.getTemplate(), n.getStatus(), n.getCreatedAt());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Notifications addressed to the current user")
    public Page<NotificationResponse> myNotifications(@AuthenticationPrincipal Jwt jwt, Pageable pageable) {
        return notificationService.findByUser(UUID.fromString(jwt.getSubject()), pageable)
                .map(NotificationResponse::from);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Full notification log (admin)")
    public Page<NotificationResponse> all(Pageable pageable) {
        return notificationService.findAll(pageable).map(NotificationResponse::from);
    }
}
