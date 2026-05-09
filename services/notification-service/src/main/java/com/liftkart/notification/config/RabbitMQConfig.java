package com.liftkart.notification.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.queues.order-notification}")
    private String orderNotificationQueue;

    @Value("${app.rabbitmq.queues.vendor-approved}")
    private String vendorApprovedQueue;

    @Value("${app.rabbitmq.queues.vendor-rejected}")
    private String vendorRejectedQueue;

    @Value("${app.rabbitmq.queues.low-stock}")
    private String lowStockQueue;

    // Declare all queues this service listens to
    // durable=true means they survive RabbitMQ restarts
    @Bean
    public Queue orderNotificationQueue() {
        return QueueBuilder.durable(orderNotificationQueue).build();
    }

    @Bean
    public Queue vendorApprovedQueue() {
        return QueueBuilder.durable(vendorApprovedQueue).build();
    }

    @Bean
    public Queue vendorRejectedQueue() {
        return QueueBuilder.durable(vendorRejectedQueue).build();
    }

    @Bean
    public Queue lowStockQueue() {
        return QueueBuilder.durable(lowStockQueue).build();
    }

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