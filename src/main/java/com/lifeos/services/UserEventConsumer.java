package com.lifeos.services;

import com.lifeos.dtos.event.UserRegisteredEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserEventConsumer {

    private final EmailService emailService;

    public UserEventConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @KafkaListener(topics = "lifeos-user-events", groupId = "lifeos-group")
    public void consumeUserRegisteredEvent(UserRegisteredEvent event) {
        System.out.println("[KAFKA CONSUMER] Received UserRegisteredEvent for email: " + event.email());
        emailService.sendWelcomeEmail(event.email(), event.firstName());
    }
}
