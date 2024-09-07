package com.appsdeveloperblog.payments.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfig {
	@Value("${payment.events.topic.name}")
	private String paymentEventTopic;
    @Bean
    public RestTemplate ccpRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }
    @Bean
	NewTopic createPaymentEventTopic() {
		return TopicBuilder.name(paymentEventTopic).partitions(3).replicas(3).build();
	}
}
