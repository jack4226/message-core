package ltj.spring.util;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.listener.AbstractMessageListenerContainer;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class SpringJmsUtil {
	protected final static Logger logger = Logger.getLogger(SpringJmsUtil.class);
	
	private static AnnotationConfigApplicationContext ctx = null;
	
	static AbstractApplicationContext getApplicationContext() {
		if (ctx == null) {
			ctx = new AnnotationConfigApplicationContext(SpringAppConfig.class);
		}
		return ctx;
	}
	
	public static AbstractMessageListenerContainer getJmsListenerContainer() {
		DefaultMessageListenerContainer container = getApplicationContext().getBean(DefaultMessageListenerContainer.class);
		return container;
	}
}