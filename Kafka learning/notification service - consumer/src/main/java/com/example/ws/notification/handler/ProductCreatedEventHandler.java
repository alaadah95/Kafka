package com.example.ws.notification.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.example.ws.core.ProductCreatedEvent;
import com.example.ws.core.TopicsNames;
import com.example.ws.core.errors.NotRetriableException;
import com.example.ws.core.errors.RetriableException;
import com.example.ws.notification.entity.ProcessedEventEntity;
import com.example.ws.notification.repository.ProcessedEventRepository;

import lombok.extern.log4j.Log4j2;

@KafkaListener(topics = TopicsNames.PRODUCT_CREATED_EVENTS_TOPIC , containerFactory = "")
@Log4j2
@Component
public class ProductCreatedEventHandler {
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Autowired
	private ProcessedEventRepository processedEventRepository;
	
	@Transactional
	@KafkaHandler
	public void handle(@Payload ProductCreatedEvent event , 
			@Header(value = "messageId" , required = false) String messageId , 
			@Header(value = KafkaHeaders.RECEIVED_KEY) String messageKey ) {
//		if(true)
//			throw new NotRetriableException("no need to retries this just go to DLT.");
		log.info("Event recieved "+event.getTitle());
		
		//to make the consumer Idempotent
		if (processedEventRepository.existsById(messageId)) {
			log.info("Event processed before ");
			return;
		}
		
		try {
			processedEventRepository.save(new ProcessedEventEntity(messageId, event.getProductID()));
		} catch (DataIntegrityViolationException e) {
			throw new NotRetriableException(e.getMessage());
		}
		
		String url = "http://localhost:8082";
		try {
		ResponseEntity<String> responseTemp = restTemplate.exchange(url, HttpMethod.GET,null,String.class);
		if(responseTemp.getStatusCode() == HttpStatus.OK)
			log.info(" Recieved response from a remote service "+responseTemp.getBody());
		}
		catch(ResourceAccessException ex)
		{
			log.error(""+ex.getMessage());
			//if exception happen it will retry again but if retries reach to max the event will go to DLT.
			throw new RetriableException(ex.getMessage());
		}
		catch (HttpServerErrorException  e) {
			log.error(""+e.getMessage());
			throw new NotRetriableException(e.getMessage());
		}
		catch (Exception e) {
			log.error(""+e.getMessage());
			throw new NotRetriableException(e.getMessage());
		} 
		
	}

	//DLT CLI
	//kafka-console-consumer --bootstrap-server broker-1:29092 --topic product-created-events-topic.DLT --from-beginning --property print.key=true --property print.value=true
}
