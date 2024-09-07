package com.appsdeveloperblog.orders.saga;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.appsdeveloperblog.core.dto.commands.ApproveOrderCommand;
import com.appsdeveloperblog.core.dto.commands.CancelProductReservationCommand;
import com.appsdeveloperblog.core.dto.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.core.dto.commands.RejectOrderCommand;
import com.appsdeveloperblog.core.dto.commands.ReserveProductCommand;
import com.appsdeveloperblog.core.dto.events.OrderApprovedEvent;
import com.appsdeveloperblog.core.dto.events.OrderCreatedEvent;
import com.appsdeveloperblog.core.dto.events.PaymentFailedEvent;
import com.appsdeveloperblog.core.dto.events.PaymentProcessedEvent;
import com.appsdeveloperblog.core.dto.events.ProductReservationCancelled;
import com.appsdeveloperblog.core.dto.events.ProductReservedEvent;
import com.appsdeveloperblog.core.types.OrderStatus;
import com.appsdeveloperblog.orders.service.OrderHistoryService;

@Component
@KafkaListener(topics = { "${orders.events.topic.name}", "${products.event.topic.name}",
		"${payment.events.topic.name}" })
public class OrderSaga {

	@Autowired
	private KafkaTemplate<String, Object> kafkaTemplate;

	@Value("${products.commands.topic.name}")
	private String productCommandsTopicName;
	
	@Value("${payment.commands.topic.name}")
	private String paymentCommandsTopicName;
	@Value("${order.commands.topic.name}")
	private String orderCommandsTopicName ;

	@Autowired
	private OrderHistoryService historyService;

	@KafkaHandler
	public void handleEvent(@Payload OrderCreatedEvent event) {
		ReserveProductCommand command = new ReserveProductCommand(event.getOrderId(), event.getProductId(),
				event.getProductQuantity());
		kafkaTemplate.send(productCommandsTopicName, command);
		historyService.add(command.getOrderId(), OrderStatus.CREATED);
	}

	@KafkaHandler
	public void handleEvent(@Payload ProductReservedEvent event) {
		ProcessPaymentCommand command = 
				new ProcessPaymentCommand(event.getOrderId(),
						event.getProductId(),
						event.getProductQuantity(),
						event.getProductPrice());
		kafkaTemplate.send(paymentCommandsTopicName, command);
	}
	@KafkaHandler
	public void handleEvent(@Payload PaymentProcessedEvent  event) {
		ApproveOrderCommand command = new ApproveOrderCommand(event.getOrderId());
		kafkaTemplate.send(orderCommandsTopicName, command);
	}

	@KafkaHandler
	public void handleEvent(@Payload OrderApprovedEvent event) {
		historyService.add(event.getOrderId(), OrderStatus.APPROVED);
	}
	
	@KafkaHandler
	public void handleEvent(@Payload PaymentFailedEvent event) {
		CancelProductReservationCommand command = new CancelProductReservationCommand(event.getOrderId(),event.getProductId(),event.getProductQuantity());
		kafkaTemplate.send(productCommandsTopicName, command);
	}
	
	@KafkaHandler
	public void handleEvent(@Payload ProductReservationCancelled event) {
		RejectOrderCommand command  = new RejectOrderCommand(event.getOrderId());
		kafkaTemplate.send(orderCommandsTopicName,command);
		historyService.add(event.getOrderId(), OrderStatus.REJECTED);
	}
}
