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
import org.springframework.context.annotation.Scope;
import org.springframework.jms.annotation.JmsListenerConfigurer;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerEndpointRegistrar;
import org.springframework.jms.config.SimpleJmsListenerEndpoint;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.jms.support.destination.DestinationResolver;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import ltj.jbatch.queue.MailSenderListener;
import ltj.tomee.util.TomeeCtxUtil;

@Configuration
public class SpringJmsConfig implements JmsListenerConfigurer {
	protected final static Logger logger = Logger.getLogger(SpringJmsConfig.class);

	private @Value("${test.Queue}") String listenerQueueName;
	
	private @Value("${listener.Queues}") String[] listenerQueues;
	
	private @Value("${mailReaderOutput.Queue}") String mailReaderOutputQueueName;
	private @Value("${ruleEngineOutput.Queue}") String ruleEngineOutputQueueName;
	private @Value("${mailSenderInput.Queue}") String mailSenderInputQueueName;
	
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
	@Scope(value="prototype")
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
	
	@Bean
	public ltj.jbatch.queue.JmsListener jmsListener() {
		return new ltj.jbatch.queue.JmsListener();
	}
	
	@Bean(initMethod="initialize", destroyMethod="destroy")
    public DefaultMessageListenerContainer jmsListenerContainer() {
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
        //container.setDestinationResolver(destinationResolver());
        // set Listener properties
        container.setAutoStartup(false);
        container.setConcurrency("1-4");
        container.setMaxConcurrentConsumers(4);
        //container.setErrorHandler(null); // TODO add error handler
        //container.setMessageSelector(null); // XXX implement
        container.setSessionTransacted(true);
        // end of properties
        container.setMessageListener(jmsListener());
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
		DestinationResolver resolver = new DynamicDestinationResolver();
		//resolver = new JndiDestinationResolver();
		return resolver;
	}
	
	@Bean
	public MailSenderListener mailSenderListener() {
		return new MailSenderListener();
	}
	
    @Override
    public void configureJmsListeners(JmsListenerEndpointRegistrar registrar) {
    	registrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    	if (listenerQueues != null && listenerQueues.length > 0) {
	    	for (String queueName : listenerQueues) {
	    		logger.info("Register JMS endpoint for queue: " + queueName);
	            SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
	            endpoint.setId(queueName + "_id");
	            endpoint.setDestination(queueName);
	            endpoint.setConcurrency("1-4");
	            if (StringUtils.equals(queueName, mailReaderOutputQueueName)) {
	            	endpoint.setMessageListener(message -> {
		                // TODO implement
		            	logger.info("Received: " + message);
		            });
	            }
	            else if (StringUtils.equals(queueName, ruleEngineOutputQueueName)) {
	            	endpoint.setMessageListener(message -> {
		                // TODO implement
		            	logger.info("Received: " + message);
		            });
	            }
	            else if (StringUtils.equals(queueName, mailSenderInputQueueName)) {
	            	endpoint.setMessageListener(mailSenderListener());
	            }
	            else {
		            endpoint.setMessageListener(message -> {
		            	// default 
		            	logger.info("Received: " + message);
		            });
	            }
	            registrar.registerEndpoint(endpoint);
	    	}
    	}
    }
    
    @Bean
    public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        //factory.setValidator(myValidator()); // TODO implement
        //factory.setMessageConverter(null); // TODO implement
        return factory;
    }
    
}
