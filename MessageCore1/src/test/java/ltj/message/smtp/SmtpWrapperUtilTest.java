package ltj.message.smtp;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import ltj.jbatch.pool.NamedPools;
import ltj.jbatch.pool.ObjectPool;
import ltj.jbatch.smtp.SmtpConnection;
import ltj.message.bo.mailsender.SmtpWrapperUtil;

public class SmtpWrapperUtilTest {
	static final Logger logger = Logger.getLogger(SmtpWrapperUtilTest.class);

	@Test
	public void test() {
		NamedPools pools = SmtpWrapperUtil.getSmtpNamedPools();
		for (String name :pools.getNames()) {
			logger.info("Pool name: " + name);
		}
		assertTrue(pools.getNames().contains("smtpServer"));
		assertTrue(pools.getNames().contains("exchServer"));
		
		List<ObjectPool> objPools = pools.getPools();
		assertEquals(2, objPools.size());
		
		int _size = 0;
		for (int i = 0; i < objPools.size(); i++) {
			ObjectPool pool = objPools.get(i);
			_size += pool.getSize();
		}
		logger.info("Total Connections: " + _size);
		assertTrue(_size >=2 );
		
		SmtpConnection[] conns = new SmtpConnection[_size];
		try {
			for (int i = 0; i < _size; i++) {
				conns[i] = (SmtpConnection) pools.getConnection();
				conns[i].testConnection(true);
			}
			assertEquals(0, pools.getPool("smtpServer").getNumberOfFreeItems());
			assertEquals(0, pools.getPool("exchServer").getNumberOfFreeItems());
			for (int i = 0; i < _size; i++) {
				pools.returnConnection(conns[i]);
			}
			assertTrue(0 < pools.getPool("smtpServer").getNumberOfFreeItems());
			assertTrue(0 < pools.getPool("exchServer").getNumberOfFreeItems());
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
