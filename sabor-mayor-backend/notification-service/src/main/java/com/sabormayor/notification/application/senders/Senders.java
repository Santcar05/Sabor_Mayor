package com.sabormayor.notification.application.senders;

public interface Senders {

    interface EmailSender extends NotificationSender {
    }

    interface SmsSender extends NotificationSender {
    }

    interface PushSender extends NotificationSender {
    }

    interface WhatsAppSender extends NotificationSender {
    }
}
