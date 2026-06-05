package com.campconnect.userservice.messaging.rabbitmq;

import com.campconnect.userservice.config.RabbitMQConfig;
import com.campconnect.userservice.event.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserEventConsumer {

    @RabbitListener(queues = RabbitMQConfig.USER_CREATED_QUEUE)
    public void handleUserCreated(UserEvent event) {
        log.info("[RabbitMQ] 📥 Reçu USER_CREATED → userId={} | email={}",
                event.getUserId(), event.getEmail());
        // Ici tu peux notifier d'autres services, envoyer un email de bienvenue, etc.
    }

    @RabbitListener(queues = RabbitMQConfig.USER_UPDATED_QUEUE)
    public void handleUserUpdated(UserEvent event) {
        log.info("[RabbitMQ] 📥 Reçu USER_UPDATED → userId={} | email={}",
                event.getUserId(), event.getEmail());
    }

    @RabbitListener(queues = RabbitMQConfig.USER_DELETED_QUEUE)
    public void handleUserDeleted(UserEvent event) {
        log.info("[RabbitMQ] 📥 Reçu USER_DELETED → userId={} | email={}",
                event.getUserId(), event.getEmail());
    }

    @RabbitListener(queues = RabbitMQConfig.PASSWORD_RESET_QUEUE)
    public void handlePasswordReset(UserEvent event) {
        log.info("[RabbitMQ] 📥 Reçu PASSWORD_RESET → userId={} | email={}",
                event.getUserId(), event.getEmail());
    }

    // ─── Dead Letter Queue ────────────────────────────────────
    @RabbitListener(queues = RabbitMQConfig.USER_DLQ)
    public void handleDeadLetter(Object message) {
        log.error("[RabbitMQ] ⚠️ Message en Dead Letter Queue : {}", message);
    }
}