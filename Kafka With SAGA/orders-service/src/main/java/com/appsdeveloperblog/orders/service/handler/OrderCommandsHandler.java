package com.appsdeveloperblog.orders.service.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.appsdeveloperblog.core.dto.commands.ApproveOrderCommand;
import com.appsdeveloperblog.core.dto.commands.RejectOrderCommand;
import com.appsdeveloperblog.orders.service.OrderService;

@Component
@KafkaListener(topics = { "${order.commands.topic.name}" })
public class OrderCommandsHandler {
	@Autowired
	private OrderService orderService;

	@KafkaHandler
	public void handleEvent(@Payload ApproveOrderCommand command) {
		orderService.approveOrder(command.getOrderId());
	}
	
	@KafkaHandler
	public void handleEvent(@Payload RejectOrderCommand command) {
		orderService.rejectOrder(command.getOrderId());
	}
}
