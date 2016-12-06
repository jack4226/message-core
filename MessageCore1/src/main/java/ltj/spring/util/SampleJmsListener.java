package ltj.spring.util;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class SampleJmsListener implements MessageListener {

	@Override
    public void onMessage(Message message) {
        System.out.println("SampleJmsListener has received: " + message);
    }
	
	public static void main(String[] args) {
		AbstractApplicationContext factory = null;
		DefaultMessageListenerContainer listener = null;
		try {
			factory = new AnnotationConfigApplicationContext(SpringAppConfig.class);
			
			listener = factory.getBean(DefaultMessageListenerContainer.class);
			
			SampleJmsListener lstr = new SampleJmsListener();
			
			listener.setDestination(new ActiveMQQueue("testQueue"));
			listener.setMessageListener(lstr);
			
			if (listener.isAutoStartup()) {
				System.out.println("Listener auto startup!");
			}
			else {
				listener.start();
				System.out.println("Listener started!");
				int idx = 0;
				while (listener.isRunning() && idx < 30) {
					idx ++;
				}
			}
			System.out.println("Listener class: " + listener.getMessageListener().getClass().getName());
			System.out.println("Listener Destination: " + listener.getDestination());
			System.out.println();
			System.out.println("Press any key to stop the Listener...");
			System.in.read();
		}
		catch (Exception e) {
			
			e.printStackTrace();
		}
		finally {
			if (listener != null) {
				listener.stop();
				listener.shutdown();
			}
			if (factory != null) {
				factory.close();
			}
		}
	}
}
