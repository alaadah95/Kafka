package com.example.ws.products.services;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.BeanUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.example.ws.core.ProductCreatedEvent;
import com.example.ws.core.TopicsNames;
import com.example.ws.products.model.CreateProductRestModel;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class ProductServiceImpl implements ProductService {
	
	KafkaTemplate<String, ProductCreatedEvent> productCreatedProducer ;
	
	
	public ProductServiceImpl(KafkaTemplate<String, ProductCreatedEvent> productCreatedProducer) {
		this.productCreatedProducer = productCreatedProducer;
	}




	@Override
	public String createProduct(CreateProductRestModel model) {
		ProductCreatedEvent event = new ProductCreatedEvent();
		BeanUtils.copyProperties(model, event);
		String productID = UUID.randomUUID().toString();
		event.setProductID(productID);
		ProducerRecord<String, ProductCreatedEvent> producerRecord = new ProducerRecord<>(TopicsNames.PRODUCT_CREATED_EVENTS_TOPIC,productID, event);
		producerRecord.headers().add("messageId", UUID.randomUUID().toString().getBytes());
		//TODO insert into database 
		CompletableFuture<SendResult<String, ProductCreatedEvent>> future	= productCreatedProducer.send(producerRecord);
		future.whenComplete((result,exception)->{
			if (exception != null)
				log.error("Failed " + exception.getMessage());
			else {
				log.info("success in partition " + result.getRecordMetadata().partition()+
						"\n in topic"+ result.getRecordMetadata().topic()+
						"\n with offset "+result.getRecordMetadata().offset());
		
			}});
		
		return productID;
	}
	
}
