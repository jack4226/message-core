package ltj.spring.util;

import static org.junit.Assert.*;

import java.sql.SQLException;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.sql.DataSource;

import org.apache.activemq.command.ActiveMQQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

import ltj.jbatch.app.MailReaderTaskExr;
import ltj.message.bean.SimpleEmailSender;
import ltj.message.dao.emailaddr.EmailAddressDao;

public class SpringAppConfigTest {
	protected final static Logger logger = LogManager.getLogger(SpringAppConfigTest.class);
	
	AbstractApplicationContext context = null;
	
	@Before
	public void setup() {
		context = new AnnotationConfigApplicationContext(SpringAppConfig.class);
	}
	
	@Test
	public void testSpingConfigBare() {
		EmailAddressDao dao1 = context.getBean(EmailAddressDao.class);
		assertNotNull(dao1);
		
		SimpleEmailSender emailsender = context.getBean(SimpleEmailSender.class);
		assertNotNull(emailsender);
		
		// test custom "thread" scope
		final LobHandler lob1 = context.getBean(LobHandler.class);
		LobHandler lob2 = context.getBean(LobHandler.class);
		assertEquals(lob1, lob2); // from same thread
		
		Thread t = new Thread() {
			@Override
			public void run() {
				LobHandler lob3 = context.getBean(LobHandler.class);
				assertNotNull(lob3);
				assertNotEquals(lob1, lob3);
			}
		};
		t.start();
	}
	
	@Test
	public void testSpringJmsConfig1() {
		AnnotationConfigApplicationContext ctx = null;
		javax.jms.Connection conn = null;
		try {
			ctx = new AnnotationConfigApplicationContext();
			ctx.register(SpringAppConfig.class, SpringJmsConfig.class);
			ctx.refresh();
			
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
	public void testSpringJmsConfig2() {
		AnnotationConfigApplicationContext ctx = null;
		javax.jms.Connection conn = null;
		try {
			ctx = new AnnotationConfigApplicationContext();
		
			// scan package
			ctx.scan(new String[] {"ltj.jbatch.queue", "ltj.jbatch.app"});
			// load various configuration classes:
			ctx.register(SpringAppConfig.class);
			ctx.register(SpringJmsConfig.class, SpringTaskConfig.class);
			ctx.refresh();
			
			DefaultJmsListenerContainerFactory factory = ctx.getBean(DefaultJmsListenerContainerFactory.class);
			assertNotNull(factory);
			
			DefaultMessageListenerContainer listener = (DefaultMessageListenerContainer) ctx.getBean("jmsListenerContainer");
			assertNotNull(listener);
			logger.info("Original listener destination: " + listener.getDestination());
			listener.setDestination(new ActiveMQQueue("newQueue"));
			logger.info("New      listener destination: " + listener.getDestination());
			
			DefaultMessageHandlerMethodFactory handler = ctx.getBean(DefaultMessageHandlerMethodFactory.class);
			assertNotNull(handler);
			
			JmsTemplate template = ctx.getBean(JmsTemplate.class);
			assertNotNull(template);
			template.setDefaultDestination(new ActiveMQQueue("testQueue"));
			
			MailReaderTaskExr taskExr = ctx.getBean(MailReaderTaskExr.class);
			assertNotNull(taskExr);
			taskExr.cancelTasks();
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
	public void testDataSourceConfig() {
		try {
			Object obj1 = context.getBean("mysqlTransactionManager");
			assertNotNull(obj1);
			assertTrue(obj1 instanceof org.springframework.jdbc.datasource.DataSourceTransactionManager);
			
			Object obj2 = context.getBean("mysqlDataSource");
			assertNotNull(obj2);
			assertTrue(obj2 instanceof org.springframework.jdbc.datasource.DriverManagerDataSource);
			DataSource ds = (DataSource) obj2;
			
			java.sql.Connection conn = null;
			try {
				conn = ds.getConnection();
				assertNotNull(conn);
				conn.setAutoCommit(false);
			} catch (SQLException e) {
				fail();
			}
			finally {
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {}
				}
			}
		}
		finally {
			
			if (context != null) {
				context.close();
			}
		}

	}
}
