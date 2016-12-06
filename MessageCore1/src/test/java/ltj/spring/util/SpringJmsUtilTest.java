package ltj.spring.util;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.jms.listener.AbstractMessageListenerContainer;

public class SpringJmsUtilTest {
	protected final static Logger logger = Logger.getLogger(SpringJmsUtilTest.class);
	
	@Test
	public void testSpringJmsUtil() {
		AbstractApplicationContext ctx =SpringJmsUtil.getApplicationContext();
		assertNotNull(ctx);
		
		AbstractMessageListenerContainer listener = SpringJmsUtil.getJmsListenerContainer();
		assertNotNull(listener);
	}

}
