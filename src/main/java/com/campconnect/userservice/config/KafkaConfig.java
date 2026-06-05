package com.campconnect.userservice.config;

import com.campconnect.userservice.event.UserEvent;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    // ─── Noms des Topics ─────────────────────────────────────
    public static final String TOPIC_USER_CREATED  = "user-created";
    public static final String TOPIC_USER_UPDATED  = "user-updated";
    public static final String TOPIC_USER_DELETED  = "user-deleted";
    public static final String TOPIC_PASSWORD_RESET = "user-password-reset";

    // ─── Dead Letter Topics ───────────────────────────────────
    public static final String TOPIC_USER_CREATED_DLT  = "user-created.DLT";
    public static final String TOPIC_USER_UPDATED_DLT  = "user-updated.DLT";

    // ─── Group ID ────────────────────────────────────────────
    public static final String GROUP_ID = "user-service-group";

    @Value("${spring.kafka.bootstrap-servers:kafka:9092}")
    private String bootstrapServers;

    // ─── Création automatique des Topics ─────────────────────
    @Bean
    public NewTopic topicUserCreated() {
        return TopicBuilder.name(TOPIC_USER_CREATED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic topicUserUpdated() {
        return TopicBuilder.name(TOPIC_USER_UPDATED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic topicUserDeleted() {
        return TopicBuilder.name(TOPIC_USER_DELETED).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic topicPasswordReset() {
        return TopicBuilder.name(TOPIC_PASSWORD_RESET).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic topicUserCreatedDlt() {
        return TopicBuilder.name(TOPIC_USER_CREATED_DLT).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic topicUserUpdatedDlt() {
        return TopicBuilder.name(TOPIC_USER_UPDATED_DLT).partitions(1).replicas(1).build();
    }

    // ─── Producer Factory ─────────────────────────────────────
    @Bean
    public ProducerFactory<String, UserEvent> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // Retry automatique
        config.put(ProducerConfig.RETRIES_CONFIG, 3);
        config.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 1000);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, UserEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ─── Consumer Factory ─────────────────────────────────────
    @Bean
    public ConsumerFactory<String, UserEvent> consumerFactory() {
        JsonDeserializer<UserEvent> deserializer = new JsonDeserializer<>(UserEvent.class);
        deserializer.setRemoveTypeHeaders(false);
        deserializer.addTrustedPackages("*");
        deserializer.setUseTypeMapperForKey(true);

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, deserializer);
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UserEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}