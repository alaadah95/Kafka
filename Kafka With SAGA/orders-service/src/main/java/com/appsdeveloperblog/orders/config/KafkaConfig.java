package com.appsdeveloperblog.orders.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

	@Value("${orders.events.topic.name}")
	public String orderTopic;
	

	@Value("${products.commands.topic.name}")
	private String productCommandsTopicName ;
	
	@Value("${payment.commands.topic.name}")
	private String paymentCommandsTopicName;
	@Value("${order.commands.topic.name}")
	private String orderCommandsTopicName ;
    @Bean
    KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
    

	@Bean
	NewTopic createOrderTopic() {
		return TopicBuilder.name(orderTopic).partitions(3).replicas(3).build();
	}
	
	@Bean
	NewTopic createProductCommandsTopic() {
		return TopicBuilder.name(productCommandsTopicName).partitions(3).replicas(3).build();
	}
	
	@Bean
	NewTopic createPaymentCommandsTopicName() {
		return TopicBuilder.name(paymentCommandsTopicName).partitions(3).replicas(3).build();
	}
	@Bean
	NewTopic createOrderCommandsTopicName() {
		return TopicBuilder.name(orderCommandsTopicName).partitions(3).replicas(3).build();
	}

}
