package ltj.message.main;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import ltj.message.bo.test.MsgOutboxBoTest;
import ltj.message.bo.test.RuleEngineTest;
import ltj.message.dao.inbox.MsgStreamDao;
import ltj.message.dao.outbox.MsgRenderedDao;
import ltj.spring.util.SpringUtil;

public class CreateAllTablesPostTest {
	protected static final Logger logger = LogManager.getLogger(CreateAllTablesPostTest.class);

	static boolean forceInsert = true;
	
	@Test
	public void insertMsgStream() {
		MsgStreamDao msgStreamDao = SpringUtil.getDaoAppContext().getBean(MsgStreamDao.class);
		if (msgStreamDao.getLastRecord() == null || forceInsert) {
			Result result = JUnitCore.runClasses(RuleEngineTest.class);
			for (Failure failure : result.getFailures()) {
				System.err.println(failure.toString());
			}
			logger.info("Rule Engine test completed.");
			if (result.getFailureCount() > 0) {
				fail("Test failed, number of failures = " + result.getFailureCount());
			}
		}
		assertNotNull(msgStreamDao.getLastRecord());
	}
	
	@Test
	public void insertMsgRendered() {
		MsgRenderedDao msgRenderedDao = SpringUtil.getDaoAppContext().getBean(MsgRenderedDao.class);
		if (msgRenderedDao.getLastRecord() == null || forceInsert) {
			Result result = JUnitCore.runClasses(MsgOutboxBoTest.class);
			for (Failure failure : result.getFailures()) {
				System.err.println(failure.toString());
			}
			logger.info("MsgOutbox BO test completed.");
			if (result.getFailureCount() > 0) {
				fail("Test failed, number of failures = " + result.getFailureCount());
			}
		}
		assertNotNull(msgRenderedDao.getLastRecord());
	}

}
