package com.campconnect.userservice.messaging.kafka;

import com.campconnect.userservice.config.KafkaConfig;
import com.campconnect.userservice.event.UserEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UserKafkaConsumer {

    @KafkaListener(
            topics = KafkaConfig.TOPIC_USER_CREATED,
            groupId = KafkaConfig.GROUP_ID
    )
    public void consumeUserCreated(
            @Payload UserEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("[Kafka] 📥 USER_CREATED → userId={} | email={} | partition={} | offset={}",
                event.getUserId(), event.getEmail(), partition, offset);
        // Logique métier : audit log, notification, etc.
    }

    @KafkaListener(
            topics = KafkaConfig.TOPIC_USER_UPDATED,
            groupId = KafkaConfig.GROUP_ID
    )
    public void consumeUserUpdated(
            @Payload UserEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("[Kafka] 📥 USER_UPDATED → userId={} | email={} | partition={} | offset={}",
                event.getUserId(), event.getEmail(), partition, offset);
    }

    @KafkaListener(
            topics = KafkaConfig.TOPIC_USER_DELETED,
            groupId = KafkaConfig.GROUP_ID
    )
    public void consumeUserDeleted(
            @Payload UserEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("[Kafka] 📥 USER_DELETED → userId={} | email={} | partition={} | offset={}",
                event.getUserId(), event.getEmail(), partition, offset);
    }

    @KafkaListener(
            topics = KafkaConfig.TOPIC_PASSWORD_RESET,
            groupId = KafkaConfig.GROUP_ID
    )
    public void consumePasswordReset(
            @Payload UserEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("[Kafka] 📥 PASSWORD_RESET → userId={} | email={} | partition={} | offset={}",
                event.getUserId(), event.getEmail(), partition, offset);
    }
}