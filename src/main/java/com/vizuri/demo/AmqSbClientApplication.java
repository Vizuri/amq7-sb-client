package com.vizuri.demo;

import javax.jms.DeliveryMode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jms.core.JmsTemplate;

@SpringBootApplication
public class AmqSbClientApplication {
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(AmqSbClientApplication.class, args);
		
		MessageProducer producer = context.getBean(MessageProducer.class);

		for (int i = 0; i < 10; i++) {
			producer.send("This is a test " + i );
		}		
	}


	public static void send(String testMessage, JmsTemplate jmsTemplate) {
		jmsTemplate.convertAndSend("VirtualTopic.FOO.Orders", testMessage, m -> {
			m.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
			m.setStringProperty("status", "A");
			m.setStringProperty("foo", "1");
			return m;
		});
	}
	


}
