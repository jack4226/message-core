package ltj.message.dao;

import static org.junit.Assert.*;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.MsgUnreadCountDao;

public class MsgUnreadCountTest extends DaoTestBase {
	@Resource
	private MsgUnreadCountDao msgUnreadCountDao;
	
	@Test
	public void testUpdate() {
		int rows = resetUnreadCounts();
		assertEquals(15, rows);
		rows = updateInboxUnreadCount(1);
		assertEquals(1, rows);
		rows = updateInboxUnreadCount(-1);
		assertEquals(1, rows);
		rows = updateSentUnreadCount(1);
		assertEquals(1, rows);
		rows = updateSentUnreadCount(-1);
		assertEquals(1, rows);
	}

	private int updateInboxUnreadCount(int delta) {
		int rowsUpdated = msgUnreadCountDao.updateInboxUnreadCount(delta);
		int count = msgUnreadCountDao.selectInboxUnreadCount();
		logger.info("updateInboxUnreadCount() " + rowsUpdated + ", count: " + count);
		return rowsUpdated;
	}
	
	private int updateSentUnreadCount(int delta) {
		int rowsUpdated = msgUnreadCountDao.updateSentUnreadCount(delta);
		int count = msgUnreadCountDao.selectSentUnreadCount();
		logger.info("updateSentUnreadCount() " + rowsUpdated + ", count: " + count);
		return rowsUpdated;
	}
	
	private int resetUnreadCounts() {
		msgUnreadCountDao.resetUnreadCounts(10, 5);
		int inboxCount = msgUnreadCountDao.selectInboxUnreadCount();
		int sentCount = msgUnreadCountDao.selectSentUnreadCount();
		logger.info("updateUnreadCounts() " + inboxCount + " : " + sentCount);
		return (inboxCount + sentCount);
	}
}
