package com.appsdeveloperblog.products.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {
	@Value("${products.event.topic.name}")
	private String productTopicName ;
	
    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
    
    @Bean
	NewTopic createProductTopicName() {
		return TopicBuilder.name(productTopicName).partitions(3).replicas(3).build();
	}
}
