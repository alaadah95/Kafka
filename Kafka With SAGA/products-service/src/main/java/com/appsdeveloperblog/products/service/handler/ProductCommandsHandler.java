package com.appsdeveloperblog.products.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.appsdeveloperblog.core.dto.Product;
import com.appsdeveloperblog.core.dto.commands.CancelProductReservationCommand;
import com.appsdeveloperblog.core.dto.commands.ReserveProductCommand;
import com.appsdeveloperblog.core.dto.events.ProductReservationCancelled;
import com.appsdeveloperblog.core.dto.events.ProductReservationFailedEvent;
import com.appsdeveloperblog.core.dto.events.ProductReservedEvent;
import com.appsdeveloperblog.core.exceptions.ProductInsufficientQuantityException;
import com.appsdeveloperblog.products.service.ProductService;

@Component
@KafkaListener(topics = { "${products.commands.topic.name}" })
public class ProductCommandsHandler {
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	ProductService productService;
	@Autowired
	KafkaTemplate<String, Object> kafkaTemplate;
	@Value("${products.event.topic.name}")
	private String productTopicName;

	@KafkaHandler
	public void handleCommand(@Payload ReserveProductCommand command) {
		try {
			Product product = new Product(command.getProductId(), command.getProductQuantity());
			Product reservedProduct = productService.reserve(product, command.getOrderId());
			ProductReservedEvent event = new ProductReservedEvent(command.getOrderId(), command.getProductId(),
					reservedProduct.getPrice(), command.getProductQuantity());
			kafkaTemplate.send(productTopicName, event);
		} catch (ProductInsufficientQuantityException e) {
			ProductReservationFailedEvent event = new ProductReservationFailedEvent(command.getOrderId(),
					command.getProductId(), command.getProductQuantity());
			kafkaTemplate.send(productTopicName, event);
		} catch (Exception e) {
			logger.error(e.getLocalizedMessage(), e);
		}
	}
	

	@KafkaHandler
	public void handleCommand(@Payload CancelProductReservationCommand command) {
		Product product = new Product(command.getProductId(), command.getProductQuantity());
		productService.cancelReservation(product, command.getOrderId());
		
		ProductReservationCancelled event = new ProductReservationCancelled(command.getOrderId(), command.getProductId());
		kafkaTemplate.send(productTopicName,event);
	}

}
