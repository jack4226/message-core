package test;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import ltj.spring.util.SpringAppConfig;

public class JmsQueueSender {

	public static void main(String[] args) {
		AbstractApplicationContext factory = new AnnotationConfigApplicationContext(SpringAppConfig.class);

		try {
			JmsQueueSender test = new JmsQueueSender();
			test.send(factory, "JmsQueueSender test text message.");
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	public void send(AbstractApplicationContext factory, final String text) {
		JmsTemplate template = (JmsTemplate) factory.getBean("jmsTemplate");
		if (template.getDefaultDestination() == null) {
			template.setDefaultDestination(new ActiveMQQueue("rmaRequestInput"));
		}

		template.send(template.getDefaultDestination(), new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(text);
			}
		});
		System.out.println("Message Sent");
	}
}