package com.liftkart.auth.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchanges.auth}")
    private String authExchange;

    @Value("${app.rabbitmq.queues.vendor-approved}")
    private String vendorApprovedQueue;

    @Value("${app.rabbitmq.queues.vendor-rejected}")
    private String vendorRejectedQueue;

    @Value("${app.rabbitmq.routing-keys.vendor-approved}")
    private String vendorApprovedRoutingKey;

    @Value("${app.rabbitmq.routing-keys.vendor-rejected}")
    private String vendorRejectedRoutingKey;

    // Exchange — the post office
    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(authExchange);
    }

    // Queues — the mailboxes
    @Bean
    public Queue vendorApprovedQueue() {
        return QueueBuilder.durable(vendorApprovedQueue).build();
    }

    @Bean
    public Queue vendorRejectedQueue() {
        return QueueBuilder.durable(vendorRejectedQueue).build();
    }

    // Bindings — connects queue to exchange via routing key
    @Bean
    public Binding vendorApprovedBinding() {
        return BindingBuilder
                .bind(vendorApprovedQueue())
                .to(authExchange())
                .with(vendorApprovedRoutingKey);
    }

    @Bean
    public Binding vendorRejectedBinding() {
        return BindingBuilder
                .bind(vendorRejectedQueue())
                .to(authExchange())
                .with(vendorRejectedRoutingKey);
    }

    // Send messages as JSON instead of raw bytes
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}