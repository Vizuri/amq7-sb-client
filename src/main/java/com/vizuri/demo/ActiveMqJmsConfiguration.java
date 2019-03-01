package com.vizuri.demo;

import java.text.SimpleDateFormat;

//import org.springframework.jms.support.converter.MessageType;


import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Session;

import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.messaginghub.pooled.jms.JmsPoolConnectionFactory;
//import org.apache.activemq.jms.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.jms.JmsPoolConnectionFactoryProperties;
import org.springframework.boot.autoconfigure.jms.activemq.ActiveMQProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;



@Configuration
@EnableJms
@EnableConfigurationProperties(ActiveMQProperties.class)
public class ActiveMqJmsConfiguration {
    static Logger logger = LoggerFactory.getLogger(ActiveMqJmsConfiguration.class);

    static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ");

    static ActiveMQConnectionFactory myConnectionFactory(ActiveMQProperties properties,
                                                         @Value("${spring.application.name}") String appName,
                                                         @Value("${CLOUD_APP_INSTANCE_NAME}") String cloudAppInstanceName,
                                                         @Value("${mq.initial.redelivery.delay}") int initialRedeliveryDelay,
                                                         @Value("${mq.backff.multiplier}") int backoffMultiplier,
                                                         @Value("${mq.use.exponential.backoff}") boolean userExponentialBackoff,
                                                         @Value("${mq.maximum.redeliveries}") int maximumReDeliveries) {
        ActiveMQConnectionFactory factory;

        if (StringUtils.hasLength(properties.getUser()) && StringUtils.hasLength(properties.getPassword()) && StringUtils.hasLength(properties.getBrokerUrl())) {
            factory = new ActiveMQConnectionFactory(properties.getUser(), properties.getPassword(), properties.getBrokerUrl());
        } else {
            factory = new ActiveMQConnectionFactory();
        }

// Commented out properties that no longer exist
        
//        //factory.setCloseTimeout(properties.getCloseTimeout());
//        factory.setCloseTimeout(60000);
//               
//        factory.setNonBlockingRedelivery(properties.isNonBlockingRedelivery());
//        
//        //factory.setSendTimeout(properties.getSendTimeout());
//        factory.setSendTimeout(60000);
//        
//        ActiveMQProperties.Packages packages = properties.getPackages();
//        if (packages.getTrustAll() != null) {
//            factory.setTrustAllPackages(packages.getTrustAll());
//        }
//        if (!packages.getTrusted()
//                .isEmpty()) {
//            factory.setTrustedPackages(packages.getTrusted());
//        }
//
//        factory.setClientIDPrefix(appName + "-" + cloudAppInstanceName);
//        factory.setProducerWindowSize(-1);
//
//        factory.setUseCompression(true);
//        factory.setAlwaysSyncSend(true);
//
//        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
//        redeliveryPolicy.setInitialRedeliveryDelay(initialRedeliveryDelay);
//        redeliveryPolicy.setBackOffMultiplier(backoffMultiplier);
//        redeliveryPolicy.setUseExponentialBackOff(userExponentialBackoff);
//        redeliveryPolicy.setMaximumRedeliveries(maximumReDeliveries);
//        factory.setRedeliveryPolicy(redeliveryPolicy);

        return factory;
    }

    @Primary
    @Bean(name = "demoApiPooledJmsConnectionFactory", initMethod = "start", destroyMethod = "stop")
    static JmsPoolConnectionFactory pooledJmsConnectionFactory(ActiveMQProperties properties,
                                                              @Value("${spring.application.name}") String appName,
                                                              @Value("${CLOUD_APP_INSTANCE_NAME}") String cloudAppInstanceName,
                                                              @Value("${mq.initial.redelivery.delay}") int initialRedeliveryDelay,
                                                              @Value("${mq.backff.multiplier}") int backoffMultiplier,
                                                              @Value("${mq.use.exponential.backoff}") boolean userExponentialBackoff,
                                                              @Value("${mq.maximum.redeliveries}") int maximumReDeliveries) {
        
    	logger.info("In Init pooledJmsConnectionFactory");
    	
// Changed to JmsPoolConnectionFactory
    	
    	JmsPoolConnectionFactory pooledConnectionFactory = new JmsPoolConnectionFactory();

        pooledConnectionFactory.setConnectionFactory(myConnectionFactory(properties, appName,
                cloudAppInstanceName,initialRedeliveryDelay,
                backoffMultiplier,userExponentialBackoff,maximumReDeliveries));
        
       
        JmsPoolConnectionFactoryProperties pool = properties.getPool();
        //ActiveMQProperties.Pool pool = properties.getPool();
        pooledConnectionFactory.setBlockIfSessionPoolIsFull(pool.isBlockIfFull());      
        //pooledConnectionFactory.setBlockIfSessionPoolIsFullTimeout(pool.getBlockIfFullTimeout());
       
       
       // pooledConnectionFactory.setCreateConnectionOnStartup(pool.isCreateConnectionOnStartup());
       // pooledConnectionFactory.setExpiryTimeout(pool.getExpiryTimeout());
      //  pooledConnectionFactory.setIdleTimeout(pool.getIdleTimeout());
        pooledConnectionFactory.setMaxConnections(pool.getMaxConnections());
      //  pooledConnectionFactory.setMaximumActiveSessionPerConnection(pool.getMaximumActiveSessionPerConnection());
      //  pooledConnectionFactory.setReconnectOnException(pool.isReconnectOnException());
      //  pooledConnectionFactory.setTimeBetweenExpirationCheckMillis(pool.getTimeBetweenExpirationCheck());
        pooledConnectionFactory.setUseAnonymousProducers(pool.isUseAnonymousProducers());
        return pooledConnectionFactory;
    }

    @Bean
    static Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.simpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ");
    }

    @Primary
    @Bean(name = "DemoApiListenerContainerFactory")
    static DefaultJmsListenerContainerFactory jmsListenerContainerFactory(@Qualifier("demoApiPooledJmsConnectionFactory") ConnectionFactory connectionFactory) {
    	DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setSessionTransacted(true);
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        factory.setErrorHandler(t -> System.err.println("An error has occurred in the transaction"));
        
 // Added new property settings for JMS 2.0       
		factory.setPubSubDomain(true);
		factory.setSubscriptionDurable(true);
		factory.setSubscriptionShared(true);

        
        return factory;
    }
   

    @Bean
    static MessageConverter jacksonJmsMessageConverter() {
    	logger.info("In jacksonJmsMessageConverter");
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);
        converter.setTypeIdPropertyName("_type");
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setDateFormat(formatter);
        converter.setObjectMapper(objectMapper);
        return converter;
    }

    @Bean(name = "JMS_TOPIC_TEMPLATE_BEAN_NAME")
    static JmsTemplate jmsTopicTemplate(@Qualifier("demoApiPooledJmsConnectionFactory") JmsPoolConnectionFactory pooledJmsConnectionFactory) {
        logger.info("Creating jmsTopicTemplate");
    	JmsTemplate jmsTemplate = new JmsTemplate(pooledJmsConnectionFactory);
        jmsTemplate.setDeliveryMode(DeliveryMode.PERSISTENT);
        jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
        jmsTemplate.setPubSubDomain(true);
        return jmsTemplate;
    }
    @Bean(name = "JMS_QUEUE_TEMPLATE_BEAN_NAME")
    static JmsTemplate jmsQueueTemplate(@Qualifier("demoApiPooledJmsConnectionFactory") JmsPoolConnectionFactory pooledJmsConnectionFactory) {
        logger.info("Creating jmsQueueTemplate");
       JmsTemplate jmsTemplate = new JmsTemplate(pooledJmsConnectionFactory);
        jmsTemplate.setMessageConverter(jacksonJmsMessageConverter());
        jmsTemplate.setDeliveryMode(DeliveryMode.PERSISTENT);
      //  jmsTemplate.setSessionTransacted(false);
        return jmsTemplate;
    }


}