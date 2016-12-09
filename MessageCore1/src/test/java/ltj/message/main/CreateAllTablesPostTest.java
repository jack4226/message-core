package ltj.message.main;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.springframework.test.annotation.Rollback;

import ltj.message.bo.test.MsgOutboxBoTest;
import ltj.message.bo.test.RuleEngineTest;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.MsgStreamDao;
import ltj.message.dao.outbox.MsgRenderedDao;

public class CreateAllTablesPostTest extends DaoTestBase {
	@Resource
	private MsgStreamDao msgStreamDao;
	@Resource
	private MsgRenderedDao msgRenderedDao;
	@Test
	@Rollback(false)
	public void insertMsgStream() {
		if (msgStreamDao.getLastRecord()==null) {
			Result result = JUnitCore.runClasses(RuleEngineTest.class);
			for (Failure failure : result.getFailures()) {
				System.err.println(failure.toString());
			}
			System.out.println("Rule Engine test completed.");
		}
		assertNotNull(msgStreamDao.getLastRecord());
	}
	@Test
	@Rollback(false)
	public void insertMsgRendered() {
		if (msgRenderedDao.getLastRecord()==null) {
			Result result = JUnitCore.runClasses(MsgOutboxBoTest.class);
			for (Failure failure : result.getFailures()) {
				System.err.println(failure.toString());
			}
			System.out.println("MsgOutbox BO test completed.");
		}
		assertNotNull(msgRenderedDao.getLastRecord());
	}

}
