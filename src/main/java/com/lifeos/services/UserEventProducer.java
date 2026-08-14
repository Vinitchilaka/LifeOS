package com.lifeos.services;

import com.lifeos.dtos.event.UserRegisteredEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventProducer {

    private static final String TOPIC = "lifeos-user-events";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public UserEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendUserRegisteredEvent(UserRegisteredEvent event) {
        System.out.println("[KAFKA PRODUCER] Publishing UserRegisteredEvent for email: " + event.email());
        try {
            kafkaTemplate.send(TOPIC, event.email(), event);
            System.out.println("[KAFKA PRODUCER] Published successfully!");
        } catch (Exception e) {
            System.err.println("[KAFKA PRODUCER ERROR] Failed to publish event: " + e.getMessage());
        }
    }
}
