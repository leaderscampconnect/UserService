package com.campconnect.userservice.messaging.kafka;

import com.campconnect.userservice.config.KafkaConfig;
import com.campconnect.userservice.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserKafkaProducer {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;

    public void sendUserCreated(UserEvent event) {
        send(KafkaConfig.TOPIC_USER_CREATED, event);
    }

    public void sendUserUpdated(UserEvent event) {
        send(KafkaConfig.TOPIC_USER_UPDATED, event);
    }

    public void sendUserDeleted(UserEvent event) {
        send(KafkaConfig.TOPIC_USER_DELETED, event);
    }

    public void sendPasswordReset(UserEvent event) {
        send(KafkaConfig.TOPIC_PASSWORD_RESET, event);
    }

    private void send(String topic, UserEvent event) {
        // Utiliser userId comme clé pour garantir l'ordre des messages d'un même user
        String key = event.getUserId() != null ? event.getUserId().toString() : "unknown";

        CompletableFuture<SendResult<String, UserEvent>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("[Kafka] ✅ Envoyé → topic={} | partition={} | offset={} | event={}",
                        topic,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset(),
                        event.getEventType());
            } else {
                log.error("[Kafka] ❌ Échec envoi → topic={} | event={} | erreur={}",
                        topic, event.getEventType(), ex.getMessage());
            }
        });
    }
}