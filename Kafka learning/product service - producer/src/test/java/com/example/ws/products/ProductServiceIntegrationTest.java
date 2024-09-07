package com.example.ws.products;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.example.ws.core.ProductCreatedEvent;
import com.example.ws.products.model.CreateProductRestModel;
import com.example.ws.products.services.ProductService;

@DirtiesContext
@TestInstance(Lifecycle.PER_CLASS)
@ActiveProfiles("test") //application-test.properties
@EmbeddedKafka(partitions = 3 , count = 3, controlledShutdown = true)
@SpringBootTest(properties = "spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}")
public class ProductServiceIntegrationTest {
	
	@Autowired
	private ProductService productService;
	
	@Autowired 
	private EmbeddedKafkaBroker embeddedKafkaBroker;
	
	@Autowired
	private Environment environment;
	
	private KafkaMessageListenerContainer<String,ProductCreatedEvent> container ;
	
	private BlockingQueue<ConsumerRecord<String, ProductCreatedEvent>>  records; 
 	
	@BeforeAll
	void setUp() {
		DefaultKafkaConsumerFactory<String,Object> consumerFactory = new DefaultKafkaConsumerFactory<>(getConsumerProperties());
		ContainerProperties containerProperties = new ContainerProperties(environment.getProperty("product-created-events-topic-name"));;
		container= new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
		records = new LinkedBlockingQueue<>();
		container.setupMessageListener((MessageListener<String, ProductCreatedEvent>) records::add);
		container.start();
		ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
	}
	
	@Test
	void testCreateProduct_whenGivenValidValueProductDetails_successfullSendsKafkaMessage() throws InterruptedException {
		//Arrange
		String title = "Iphone";
		String price = "600";
		Integer quantity = 1 ;
		CreateProductRestModel createProductRestModel = new CreateProductRestModel();
		createProductRestModel.setPrice(price);
		createProductRestModel.setTitle(title);
		createProductRestModel.setQuantity(String.valueOf(quantity));
		//act
		productService.createProduct(createProductRestModel);
		//assert
		ConsumerRecord<String, ProductCreatedEvent> consumerRecord = records.poll(3000,TimeUnit.MILLISECONDS);
		assertNotNull(consumerRecord);
		assertNotNull(consumerRecord.key());
		ProductCreatedEvent event  = consumerRecord.value();
		assertEquals(event.getQuantity(), createProductRestModel.getQuantity());
		
	}
	
	private Map<String, Object> getConsumerProperties(){
		return Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,embeddedKafkaBroker.getBrokersAsString(),
				ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
				ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
				ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class,
				ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.group-id"),
				JsonDeserializer.TRUSTED_PACKAGES,
				environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"),
				ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,environment.getProperty("spring.kafka.consumer.auto-offset-reset"));
	}
	
	@AfterAll
	public void shutdownContainer() {
		container.stop();
	}
}
