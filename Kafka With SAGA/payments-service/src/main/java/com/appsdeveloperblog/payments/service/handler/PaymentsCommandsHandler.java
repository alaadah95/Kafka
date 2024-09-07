package com.appsdeveloperblog.payments.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.appsdeveloperblog.core.dto.Payment;
import com.appsdeveloperblog.core.dto.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.core.dto.events.PaymentFailedEvent;
import com.appsdeveloperblog.core.dto.events.PaymentProcessedEvent;
import com.appsdeveloperblog.core.exceptions.CreditCardProcessorUnavailableException;
import com.appsdeveloperblog.payments.service.PaymentService;

@Component
@KafkaListener(topics = { "${payment.commands.topic.name}" })
public class PaymentsCommandsHandler {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PaymentService paymentService;

	@Autowired
	KafkaTemplate<String, Object> kafkaTemplate;

	@Value("${payment.events.topic.name}")
	private String paymentEventTopic;

	@KafkaHandler
	public void handleCommand(@Payload ProcessPaymentCommand command) {
		try {
			Payment payment = new Payment(command.getOrderId(), command.getProductId(), command.getProductPrice(),
					command.getProductQuantity());
			Payment processedPayment = paymentService.process(payment);
			PaymentProcessedEvent event = new PaymentProcessedEvent(processedPayment.getOrderId(),
					processedPayment.getId());
			kafkaTemplate.send(paymentEventTopic, event);
		} catch (CreditCardProcessorUnavailableException e) {
			logger.error(e.getLocalizedMessage());
			PaymentFailedEvent event = new PaymentFailedEvent(command.getOrderId(),
					command.getProductId(),
					command.getProductQuantity());
			kafkaTemplate.send(paymentEventTopic, event);
		}
	}
}
