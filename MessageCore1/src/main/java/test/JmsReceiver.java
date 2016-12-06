package test;

import javax.jms.Message;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.core.JmsTemplate;

import ltj.spring.util.SpringAppConfig;

public class JmsReceiver {

	public static void main(String[] args){
		AbstractApplicationContext factory = null;
		try {
			factory = new AnnotationConfigApplicationContext(SpringAppConfig.class);
			
			JmsReceiver test = new JmsReceiver();
			
			test.setJmsTemplate(factory.getBean(JmsTemplate.class));
			
			for (int i=0; i<5; i++) {
				test.receive();
			}
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

	private void receive() {
		if (jmsTemplate.getDefaultDestination() == null) {
			jmsTemplate.setDefaultDestination(new ActiveMQQueue("testQueue"));
		}
		if (jmsTemplate.getReceiveTimeout() == 0) {
			jmsTemplate.setReceiveTimeout(1000L);
		}
		System.out.println("Will wait "+(this.jmsTemplate.getReceiveTimeout()/1000)+" seconds...");
		
		Message msg = this.jmsTemplate.receive(this.jmsTemplate.getDefaultDestination());
		System.out.println("Received Message: "+msg);
		//System.out.println(jmsTemplate.isSessionTransacted());
	}
}