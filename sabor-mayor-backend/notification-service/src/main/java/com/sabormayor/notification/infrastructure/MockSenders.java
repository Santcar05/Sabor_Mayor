package com.sabormayor.notification.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.sabormayor.notification.application.senders.Senders;

/** Dev adapters: log instead of calling external providers. */
public final class MockSenders {

    private MockSenders() {
    }

    @Component
    @Profile("!prod")
    public static class LogEmailSender implements Senders.EmailSender {
        private static final Logger log = LoggerFactory.getLogger(LogEmailSender.class);

        @Override
        public boolean send(String recipient, String subject, String body) {
            log.info("[MOCK EMAIL] to={} subject='{}'", recipient, subject);
            return true;
        }
    }

    @Component
    @Profile("!prod")
    public static class LogSmsSender implements Senders.SmsSender {
        private static final Logger log = LoggerFactory.getLogger(LogSmsSender.class);

        @Override
        public boolean send(String recipient, String subject, String body) {
            log.info("[MOCK SMS] to={} body='{}'", recipient, body);
            return true;
        }
    }

    @Component
    @Profile("!prod")
    public static class LogPushSender implements Senders.PushSender {
        private static final Logger log = LoggerFactory.getLogger(LogPushSender.class);

        @Override
        public boolean send(String recipient, String subject, String body) {
            log.info("[MOCK PUSH] to={} title='{}'", recipient, subject);
            return true;
        }
    }

    @Component
    @Profile("!prod")
    public static class LogWhatsAppSender implements Senders.WhatsAppSender {
        private static final Logger log = LoggerFactory.getLogger(LogWhatsAppSender.class);

        @Override
        public boolean send(String recipient, String subject, String body) {
            log.info("[MOCK WHATSAPP] to={} body='{}'", recipient, body);
            return true;
        }
    }
}
