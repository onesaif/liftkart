package com.liftkart.cart.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${app.rabbitmq.exchanges.cart}")
    private String cartExchange;

    @Value("${app.rabbitmq.queues.cart-checkout}")
    private String cartCheckoutQueue;

    @Value("${app.rabbitmq.routing-keys.cart-checkout}")
    private String cartCheckoutRoutingKey;

    @Bean
    public TopicExchange cartExchange() {
        return new TopicExchange(cartExchange);
    }

    @Bean
    public Queue cartCheckoutQueue() {
        return QueueBuilder.durable(cartCheckoutQueue).build();
    }

    @Bean
    public Binding cartCheckoutBinding() {
        return BindingBuilder
                .bind(cartCheckoutQueue())
                .to(cartExchange())
                .with(cartCheckoutRoutingKey);
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