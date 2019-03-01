package com.vizuri.demo;

import javax.jms.Message;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Component;

@Component
public class MessageListener implements MessagingServiceAmqListener {

	Logger logger = LoggerFactory.getLogger(MessageListener.class);

	@Autowired
	public MessageListener() {
	}
// Updated JmsListener Properties
	@JmsListener(destination = "FOO.test_topic", 
			concurrency = "3", 
			selector = "(status = 'A') AND (foo = '1')", 
			subscription = "MySub1", 
			containerFactory = "DemoApiListenerContainerFactory")
	public void receiveInstrumentQueue(@Headers MessageHeaders headers, Message message, Session session)
			throws Exception {
		try {
			String payload = getJsonRequestMessage(message);
			logger.info("Received Message-> " + payload);
			session.commit();
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			session.commit();
		} finally {
		}
	}
}