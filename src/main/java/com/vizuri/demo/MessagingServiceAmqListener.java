package com.vizuri.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

public interface MessagingServiceAmqListener {

    Logger logger = LoggerFactory.getLogger(MessagingServiceAmqListener.class);

    default String getJsonRequestMessage(final Message message) throws Exception {

        if (message instanceof TextMessage) {
            try {
            	TextMessage tm = (TextMessage) message;
                return tm.getText();
            } catch (JMSException e) {
                logger.error("Unsupported Message Type: " + message.toString());
                throw new Exception("Unsupported Message Type: " + message.toString());
            }
        } else {
            logger.error("Unsupported Message Type: " + message.toString());
            throw new Exception("Unsupported Message Type: " + message.toString());
        }
    }


}