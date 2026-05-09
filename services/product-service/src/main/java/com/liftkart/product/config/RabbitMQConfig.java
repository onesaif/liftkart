package com.liftkart.product.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchanges.product}")
    private String productExchange;

    @Value("${app.rabbitmq.queues.low-stock}")
    private String lowStockQueue;

    @Value("${app.rabbitmq.routing-keys.low-stock}")
    private String lowStockRoutingKey;

    @Bean
    public TopicExchange productExchange() {
        return new TopicExchange(productExchange);
    }

    @Bean
    public Queue lowStockQueue() {
        return QueueBuilder.durable(lowStockQueue).build();
    }

    @Bean
    public Binding lowStockBinding() {
        return BindingBuilder
                .bind(lowStockQueue())
                .to(productExchange())
                .with(lowStockRoutingKey);
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