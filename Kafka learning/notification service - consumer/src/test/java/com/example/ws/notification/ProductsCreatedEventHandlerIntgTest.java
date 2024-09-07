package com.example.ws.notification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.Uuid;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.web.client.RestTemplate;

import com.example.ws.core.ProductCreatedEvent;
import com.example.ws.notification.entity.ProcessedEventEntity;
import com.example.ws.notification.handler.ProductCreatedEventHandler;
import com.example.ws.notification.repository.ProcessedEventRepository;

@EmbeddedKafka
@SpringBootTest(properties = "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class ProductsCreatedEventHandlerIntgTest {
	
	@MockBean
	ProcessedEventRepository eventRepository;
	@MockBean
	RestTemplate restTemplate ;
	@Autowired
	KafkaTemplate<String, Object> kafkaTemplate;
	
	@SpyBean
	ProductCreatedEventHandler createdEventHandler;
	
	@Test
	void testProducerCreatedEventHandler_onProductCreated_HandlerEvent() throws InterruptedException, ExecutionException {
		
		//Arrange
		ProductCreatedEvent createdEvent = 	new ProductCreatedEvent();
		createdEvent.setPrice("10");
		createdEvent.setProductID(Uuid.randomUuid().toString());
		createdEvent.setQuantity("5");
		createdEvent.setTitle("Test product");
		
		String messageId = UUID.randomUUID().toString();
		String messageKey = createdEvent.getProductID();
		
		ProducerRecord<String, Object> record = new ProducerRecord<String, Object>("product-created-events-topic", 
				messageKey,createdEvent);
		record.headers().add("messageId",messageId.getBytes());
		record.headers().add(KafkaHeaders.RECEIVED_KEY,messageKey.getBytes());
		
		when(eventRepository.existsById(anyString())).thenReturn(true);
		when(eventRepository.save(any(ProcessedEventEntity.class))).thenReturn(null);
		String responseBody = "{\"key\":\"value\"}";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ResponseEntity<String> responseEntity = new ResponseEntity<String>(responseBody,headers,HttpStatus.OK);
		when(restTemplate.exchange(any(String.class), any(HttpMethod.class),isNull(),eq(String.class))).thenReturn(responseEntity);
		//Act
		kafkaTemplate.send(record).get();
		//Assert
		ArgumentCaptor<String> messageIdCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> messageKeyCaptor = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<ProductCreatedEvent> enventCaptor = ArgumentCaptor.forClass(ProductCreatedEvent.class);
		
		verify(createdEventHandler, timeout(5000).times(1)).handle(enventCaptor.capture(), messageIdCaptor.capture(),
				messageKeyCaptor.capture());
		assertEquals(messageId, messageIdCaptor.getValue());
		assertEquals(messageKey, messageKeyCaptor.getValue());
		assertEquals(createdEvent, enventCaptor.getValue());
		
		
	}
}
