package ltj.spring.util;

import javax.jms.ConnectionFactory;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import ltj.tomee.util.TomeeCtxUtil;

@Configuration
//@org.springframework.context.annotation.Import(JBatchConfig.class)
@ImportResource("classpath:/properties-config.xml")
@EnableJms
public class SpringAppConfig implements JmsListenerConfigurer {
	protected final static Logger logger = Logger.getLogger(SpringAppConfig.class);

	private @Value("${Listener.QueueName}") String listenerQueueName;
	
	private @Value("${listener.Queues}") String[] listenerQueues;
	
	@Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
		DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        try {
			factory.setConnectionFactory(connectionFactory());
		} catch (NamingException e) {
			logger.error("NamingException caught", e);
			throw new java.lang.IllegalStateException("NamingException caught", e);
		}
        factory.setDestinationResolver(destinationResolver());
        factory.setConcurrency("3-10");
        return factory;
    }
	
	@Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate();
        //jmsTemplate.setDefaultDestination(new ActiveMQQueue("jms.queue"));
        try {
			jmsTemplate.setConnectionFactory(connectionFactory());
			jmsTemplate.setDestinationResolver(destinationResolver());
			jmsTemplate.setReceiveTimeout(10000L); // 10 seconds
		} catch (NamingException e) {
			logger.error("NamingException caught", e);
			throw new java.lang.IllegalStateException("NamingException caught", e);
		}
        return jmsTemplate;
    }
	
	@Bean(initMethod="initialize", destroyMethod="destroy")
    public DefaultMessageListenerContainer messageListenerContainer() {
        DefaultMessageListenerContainer container = new DefaultMessageListenerContainer();
        try {
			container.setConnectionFactory(connectionFactory());
		} catch (NamingException e) {
			logger.error("NamingException caught", e);
			throw new java.lang.IllegalStateException("NamingException caught", e);
		}
        String queueName = "testQueue";
        if (StringUtils.isNotBlank(listenerQueueName)) {
        	queueName = listenerQueueName;
        }
        container.setDestination(new ActiveMQQueue(queueName));
        //container.setMessageListener(jmsReceiver());
        return container;
    }
	
	@Bean
	//@org.springframework.context.annotation.Scope("prototype")
	public ConnectionFactory connectionFactory() throws NamingException {
		Context ctx = TomeeCtxUtil.getActiveMQContext(new String[] {});
		ConnectionFactory cf = (ConnectionFactory) ctx.lookup("ConnectionFactory");
		logger.info("ConnectionFactory instance: " + cf);
		return cf;
	}
	
	DestinationResolver destinationResolver() {
		DestinationResolver destResolver = new DynamicDestinationResolver();
		return destResolver;
	}
	
    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
    	registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    	if (listenerQueues != null) {
	    	for (String queueName : listenerQueues) {
	            SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
	            endpoint.setId(queueName + "_id");
	            endpoint.setDestination(queueName);
	            endpoint.setMessageListener(message -> {
	                // TODO processing
	            	logger.info("Received: " + message);
	            });
	            registrar.registerEndpoint(endpoint);
	    	}
    	}
        SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
        endpoint.setId("myJmsEndpoint");
        endpoint.setDestination("anotherQueue");
        endpoint.setMessageListener(message -> {
            // TODO processing
        	logger.info("Received: " + message);
        });
        registrar.registerEndpoint(endpoint);
    }
    
    @Bean
    public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        //factory.setValidator(myValidator());
        return factory;
    }
    
}
