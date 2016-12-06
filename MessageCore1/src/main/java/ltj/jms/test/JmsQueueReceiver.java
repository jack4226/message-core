package ltj.jms.test;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.core.JmsTemplate;

import com.legacytojava.message.bean.MessageBean;

import ltj.spring.util.SpringAppConfig;

public class JmsQueueReceiver {

	public static void main(String[] args) {
		AbstractApplicationContext factory = new AnnotationConfigApplicationContext(SpringAppConfig.class);

		try {
			JmsQueueReceiver test = new JmsQueueReceiver();
			for (int i = 0; i < 1; i++) {
				test.receive(factory);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	private void receive(AbstractApplicationContext factory) throws JMSException {
		JmsTemplate template = (JmsTemplate) factory.getBean("jmsTemplate");
		if (template.getDefaultDestination() == null) {
			template.setDefaultDestination(new ActiveMQQueue("rmaRequestInput"));
		}

		System.out.println("Will wait " + (template.getReceiveTimeout() / 1000) + " seconds...");

		Message msg = template.receive(template.getDefaultDestination());
		if (msg != null) {
			System.out.println("Message class name: " + msg.getClass().getName());
			System.out.println("Received Message: " + msg);
		}
		
		if (msg instanceof ObjectMessage) {
			MessageBean msgBean = (MessageBean) ((ObjectMessage) msg).getObject();
			System.out.println("MessageBean received: " + msgBean);
		}
		else if (msg instanceof TextMessage) {
			System.out.println("Text Message received: " + ((TextMessage)msg).getText());
		}
	}
}