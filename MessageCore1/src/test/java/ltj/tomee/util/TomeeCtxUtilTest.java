package ltj.tomee.util;

import static org.junit.Assert.*;

import javax.naming.Context;

import org.apache.log4j.Logger;
import org.junit.Test;

public class TomeeCtxUtilTest {
	protected final static Logger logger = Logger.getLogger(TomeeCtxUtilTest.class);
	
	@Test
	public void testTomeeCtxUtil() {
		try {
			int port = TomeeCtxUtil.findHttpPort(new int[] {8181,8080, 8161});
			logger.info("port found: " + port);
			assertTrue(port==8080 || port==8181 || port==8161);
			// test EJB remote access
			if (port != 8161) { // Not ActiveMQ Broker
				Context ctx1 = TomeeCtxUtil.getRemoteContext();
				TomeeCtxUtil.listContext(ctx1, "");
			}
		}
		catch (Exception e) {
			logger.error("Exception", e);
			fail();
		}
	}
	
	@Test
	public void testActveMqBroker() {
		try {
			Context ctx2 = TomeeCtxUtil.getActiveMQContext(new String[] {"testQueue"});
			TomeeCtxUtil.listContext(ctx2, "");
		}
		catch (Exception e) {
			logger.error("Exception", e);
			fail();
		}
	}

}
