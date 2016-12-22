package ltj.jms.test;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import ltj.spring.util.SpringUtil;

public class JmsSender {

	public static void main(String[] args) {
		AbstractApplicationContext factory = null;
		try {
			factory = SpringUtil.getAppContext();
			
			JmsSender test = new JmsSender();
			
			test.setJmsTemplate(factory.getBean(JmsTemplate.class));
			
			test.send("Test JMS text message.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (factory != null) {
				factory.close();
			}
		}
		System.exit(0);
	}

	private JmsTemplate jmsTemplate;

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}

	public void send(final String text) {
		if (jmsTemplate.getDefaultDestination() == null) {
			jmsTemplate.setDefaultDestination(new ActiveMQQueue("testQueue"));
		}
		jmsTemplate.send(jmsTemplate.getDefaultDestination(), new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(text);
			}
		});
		System.out.println("Message Sent");
	}
}