package ltj.jms.test;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.core.MessagePostProcessor;

import ltj.spring.util.SpringUtil;

public class JmsQueueSender {

	public static void main(String[] args) {
		AbstractApplicationContext factory = SpringUtil.getAppContext();
		try {
			JmsQueueSender test = new JmsQueueSender();
			test.send(factory, "JmsQueueSender test text message.");
			test.sendWithConversion(factory);
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
		System.out.println("Text Message Sent");
	}
	
	public void sendWithConversion(AbstractApplicationContext factory) {
	    Map<String, Object> map = new HashMap<>();
	    map.put("Name", "Mark");
	    map.put("Age", new Integer(47));
	    JmsTemplate template = (JmsTemplate) factory.getBean("jmsTemplate");
	    template.convertAndSend(template.getDefaultDestination(), map, new MessagePostProcessor() {
	        public Message postProcessMessage(Message message) throws JMSException {
	            message.setIntProperty("AccountID", 1234);
	            message.setJMSCorrelationID("123-00001");
	            return message;
	        }
	    });
	    System.out.println("Map Message Sent");
	    /*
	     This results in a message of the form:

			MapMessage={
				Header={
					... standard headers ...
					CorrelationID={123-00001}
				}
				Properties={
					AccountID={Integer:1234}
				}
				Fields={
					Name={String:Mark}
					Age={Integer:47}
				}
			}
	     */
	}
}