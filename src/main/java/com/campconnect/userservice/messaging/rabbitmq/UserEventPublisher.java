package com.campconnect.userservice.messaging.rabbitmq;

import com.campconnect.userservice.config.RabbitMQConfig;
import com.campconnect.userservice.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserCreated(UserEvent event) {
        publish(RabbitMQConfig.ROUTING_USER_CREATED, event);
    }

    public void publishUserUpdated(UserEvent event) {
        publish(RabbitMQConfig.ROUTING_USER_UPDATED, event);
    }

    public void publishUserDeleted(UserEvent event) {
        publish(RabbitMQConfig.ROUTING_USER_DELETED, event);
    }

    public void publishPasswordReset(UserEvent event) {
        publish(RabbitMQConfig.ROUTING_PASSWORD_RESET, event);
    }

    private void publish(String routingKey, UserEvent event) {
        try {
            log.info("[RabbitMQ] Publication → routingKey={} | event={}", routingKey, event.getEventType());
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.USER_EXCHANGE,
                    routingKey,
                    event
            );
            log.info("[RabbitMQ] ✅ Événement publié avec succès : {}", event.getEventType());
        } catch (Exception e) {
            log.error("[RabbitMQ] ❌ Erreur publication {} : {}", event.getEventType(), e.getMessage());
        }
    }
}