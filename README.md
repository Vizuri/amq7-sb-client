### Changes Needed To Upgrade to AMQ 7

## POM Changes

Updated Spring Boot Parent POM from 1.5.9.RELEASE to 2.1.1.RELEASE

From:
```
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>1.5.9.RELEASE</version>
		<relativePath /> <!-- lookup parent from repository -->
	</parent>
```
To:
```
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.1.1.RELEASE</version>
		<relativePath /> <!-- lookup parent from repository -->
	</parent>
```

Update ActiveMQ Dependencies to Artemis Dependencies

From
```
        <!-- activemq -->
        <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>activemq-client</artifactId>
            <version>5.11.0.redhat-630310</version>
        </dependency>
        <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>activemq-pool</artifactId>
            <version>5.11.0.redhat-630310</version>
        </dependency>
        <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>activemq-broker</artifactId>
            <version>5.11.0.redhat-630310</version>
        </dependency>
        <dependency>
            <groupId>org.apache.activemq</groupId>
            <artifactId>activemq-jms-pool</artifactId>
            <version>5.11.0.redhat-630310</version>
        </dependency>
        <!-- activemq -->
```
To
```
        <!-- activemq -->   
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-artemis</artifactId>
		</dependency>
		<dependency>
			<groupId>org.messaginghub</groupId>
			<artifactId>pooled-jms</artifactId>
		</dependency>
		<!-- activemq -->
```

##* Code Changes

Added settings to the DefaultJmsListenerContainerFactory

```
    @Primary
    @Bean(name = "DemoApiListenerContainerFactory")
    static DefaultJmsListenerContainerFactory jmsListenerContainerFactory(@Qualifier("demoApiPooledJmsConnectionFactory") ConnectionFactory connectionFactory) {
    	DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setSessionTransacted(true);
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        factory.setErrorHandler(t -> System.err.println("An error has occurred in the transaction"));
        
        
		factory.setPubSubDomain(true);
		factory.setSubscriptionDurable(true);
		factory.setSubscriptionShared(true);

        
        return factory;
    }
```

Switched to the JmsPoolConnectionFactory from PooledConnectionFactory in the demoApiPooledJmsConnectionFactory

```
   	JmsPoolConnectionFactory pooledConnectionFactory = new JmsPoolConnectionFactory();
```

Commented out properties that are no longer available in various places in ActiveMqJmsConfiguation.java

Update MessageListener.java with new settings.

From
```
    @JmsListener(destination = "Consumer.A.VirtualTopic.FOO.test_topic",
            concurrency = "3",
            selector = "(status = 'A') AND (foo = '1')",
            containerFactory = "DemoApiListenerContainerFactory")
    public void receiveInstrumentQueue(@Headers MessageHeaders headers, Message message, Session session) throws Exception 
```
To
```
	@JmsListener(destination = "FOO.test_topic", 
			concurrency = "3", 
			selector = "(status = 'A') AND (foo = '1')", 
			subscription = "MySub1", 
			containerFactory = "DemoApiListenerContainerFactory")
	public void receiveInstrumentQueue(@Headers MessageHeaders headers, Message message, Session session)
```
	
