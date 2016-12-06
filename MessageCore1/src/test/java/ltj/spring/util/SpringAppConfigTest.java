package ltj.spring.util;

import static org.junit.Assert.*;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

public class SpringAppConfigTest {
	protected final static Logger logger = Logger.getLogger(SpringAppConfigTest.class);
	
	@Test
	public void testSpringAppConfig1() {
		AbstractApplicationContext ctx = null;
		Connection conn = null;
		try {
			ctx = new AnnotationConfigApplicationContext(SpringAppConfig.class);
		
			ConnectionFactory factory = ctx.getBean(ConnectionFactory.class);
			assertNotNull(factory);
			
			conn = factory.createConnection();
			assertNotNull(conn);
		} catch (JMSException e) {
			logger.error("JMSException", e);
			fail();
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (JMSException e) {
					// ignore
				}
			}
			if (ctx != null) {
				ctx.close();
			}
		}
	}
	
	@Test
	public void testSpringAppConfig2() {
		AnnotationConfigApplicationContext ctx = null;
		Connection conn = null;
		try {
			ctx = new AnnotationConfigApplicationContext(); //SpringAppConfig.class);
		
			// load various configuration classes:
			ctx.register(SpringAppConfig.class);
			//ctx.register(AdditionalConfig.class, OtherConfig.class);
			ctx.refresh();
			
			DefaultJmsListenerContainerFactory factory = ctx.getBean(DefaultJmsListenerContainerFactory.class);
			assertNotNull(factory);
			
			DefaultMessageListenerContainer listener = ctx.getBean(DefaultMessageListenerContainer.class);
			assertNotNull(listener);
			logger.info("Original listener destination: " + listener.getDestination());
			listener.setDestination(new ActiveMQQueue("newQueue"));
			logger.info("New      listener destination: " + listener.getDestination());
			
			DefaultMessageHandlerMethodFactory handler = ctx.getBean(DefaultMessageHandlerMethodFactory.class);
			assertNotNull(handler);
			
			JmsTemplate template = ctx.getBean(JmsTemplate.class);
			assertNotNull(template);
			template.setDefaultDestination(new ActiveMQQueue("testQueue"));
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (JMSException e) {
					// ignore
				}
			}
			if (ctx != null) {
				ctx.close();
			}
		}
	}
}
