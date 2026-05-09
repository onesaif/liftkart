package com.liftkart.product.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${app.kafka.topics.product-views}")
    private String productViewsTopic;

    @Value("${app.kafka.topics.product-events}")
    private String productEventsTopic;

    @Bean
    public NewTopic productViewsTopic() {
        return TopicBuilder.name(productViewsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic productEventsTopic() {
        return TopicBuilder.name(productEventsTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}