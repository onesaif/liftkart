package com.liftkart.order.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchanges.order}")
    private String orderExchange;

    @Value("${app.rabbitmq.queues.order-notification}")
    private String orderNotificationQueue;

    @Value("${app.rabbitmq.queues.order-inventory}")
    private String orderInventoryQueue;

    @Value("${app.rabbitmq.routing-keys.order-notification}")
    private String orderNotificationRoutingKey;

    @Value("${app.rabbitmq.routing-keys.order-inventory}")
    private String orderInventoryRoutingKey;

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(orderExchange);
    }

    @Bean
    public Queue orderNotificationQueue() {
        return QueueBuilder.durable(orderNotificationQueue).build();
    }

    @Bean
    public Queue orderInventoryQueue() {
        return QueueBuilder.durable(orderInventoryQueue).build();
    }

    @Bean
    public Binding orderNotificationBinding() {
        return BindingBuilder.bind(orderNotificationQueue())
                .to(orderExchange()).with(orderNotificationRoutingKey);
    }

    @Bean
    public Binding orderInventoryBinding() {
        return BindingBuilder.bind(orderInventoryQueue())
                .to(orderExchange()).with(orderInventoryRoutingKey);
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