package com.sabormayor.notification.application.senders;

/**
 * One implementation per channel. Dev profiles use logging mocks; production
 * adapters (SendGrid/SES, Twilio, FCM, WhatsApp Business API) implement the
 * same contracts behind the "prod" profile.
 */
public interface NotificationSender {

    boolean send(String recipient, String subject, String body);
}
