package com.campconnect.userservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // ─── Noms des Exchanges ──────────────────────────────────
    public static final String USER_EXCHANGE = "campconnect.user.exchange";

    // ─── Noms des Queues ─────────────────────────────────────
    public static final String USER_CREATED_QUEUE  = "campconnect.user.created.queue";
    public static final String USER_UPDATED_QUEUE  = "campconnect.user.updated.queue";
    public static final String USER_DELETED_QUEUE  = "campconnect.user.deleted.queue";
    public static final String PASSWORD_RESET_QUEUE = "campconnect.user.password.reset.queue";

    // ─── Dead Letter Queue (messages en erreur) ───────────────
    public static final String USER_DLQ = "campconnect.user.dlq";
    public static final String USER_DLX = "campconnect.user.dlx";

    // ─── Routing Keys ────────────────────────────────────────
    public static final String ROUTING_USER_CREATED  = "user.created";
    public static final String ROUTING_USER_UPDATED  = "user.updated";
    public static final String ROUTING_USER_DELETED  = "user.deleted";
    public static final String ROUTING_PASSWORD_RESET = "user.password.reset";

    // ─── Exchange principal (Topic pour flexibilité) ──────────
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    // ─── Dead Letter Exchange ────────────────────────────────
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(USER_DLX);
    }

    // ─── Queues avec Dead Letter ──────────────────────────────
    @Bean
    public Queue userCreatedQueue() {
        return QueueBuilder.durable(USER_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", USER_DLX)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    @Bean
    public Queue userUpdatedQueue() {
        return QueueBuilder.durable(USER_UPDATED_QUEUE)
                .withArgument("x-dead-letter-exchange", USER_DLX)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    @Bean
    public Queue userDeletedQueue() {
        return QueueBuilder.durable(USER_DELETED_QUEUE)
                .withArgument("x-dead-letter-exchange", USER_DLX)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    @Bean
    public Queue passwordResetQueue() {
        return QueueBuilder.durable(PASSWORD_RESET_QUEUE)
                .withArgument("x-dead-letter-exchange", USER_DLX)
                .withArgument("x-dead-letter-routing-key", "dlq")
                .build();
    }

    // ─── Dead Letter Queue ────────────────────────────────────
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(USER_DLQ).build();
    }

    // ─── Bindings (lier queues à l'exchange) ──────────────────
    @Bean
    public Binding bindingUserCreated() {
        return BindingBuilder.bind(userCreatedQueue())
                .to(userExchange())
                .with(ROUTING_USER_CREATED);
    }

    @Bean
    public Binding bindingUserUpdated() {
        return BindingBuilder.bind(userUpdatedQueue())
                .to(userExchange())
                .with(ROUTING_USER_UPDATED);
    }

    @Bean
    public Binding bindingUserDeleted() {
        return BindingBuilder.bind(userDeletedQueue())
                .to(userExchange())
                .with(ROUTING_USER_DELETED);
    }

    @Bean
    public Binding bindingPasswordReset() {
        return BindingBuilder.bind(passwordResetQueue())
                .to(userExchange())
                .with(ROUTING_PASSWORD_RESET);
    }

    @Bean
    public Binding bindingDlq() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("dlq");
    }

    // ─── Convertisseur JSON ───────────────────────────────────
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ─── RabbitTemplate avec JSON ─────────────────────────────
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    // ─── Listener Factory avec JSON ───────────────────────────
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}