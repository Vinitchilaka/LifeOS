package com.lifeos.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class MailServiceHealthIndicator implements HealthIndicator {

    @Value("${spring.mail.username:YOUR_GMAIL_ADDRESS}")
    private String smtpUser;

    @Override
    public Health health() {
        if ("YOUR_GMAIL_ADDRESS".equalsIgnoreCase(smtpUser) || smtpUser.isBlank()) {
            return Health.down()
                    .withDetail("reason", "SMTP username is using placeholder values.")
                    .withDetail("configuredUser", smtpUser)
                    .build();
        }
        return Health.up()
                .withDetail("reason", "SMTP credentials successfully set.")
                .withDetail("configuredUser", smtpUser)
                .build();
    }
}
