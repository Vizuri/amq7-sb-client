package com.vizuri.demo;

import javax.jms.DeliveryMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageProducer {
	@Autowired
	@Qualifier("JMS_TOPIC_TEMPLATE_BEAN_NAME")
	JmsTemplate jmsTemplate;


	@Value("FOO.test_topic")
	String destinationQueue;

	public void send(String msg) {
		System.out.println("Sending Message:" + msg);


		jmsTemplate.convertAndSend(destinationQueue, msg, m -> {
			m.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
			m.setStringProperty("status", "A");
			m.setStringProperty("foo", "1");

			return m;
		});
	}
}