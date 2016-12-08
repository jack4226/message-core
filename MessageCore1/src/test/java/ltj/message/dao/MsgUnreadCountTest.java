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
		assertTrue(rows==15);
		rows = updateInboxUnreadCount(1);
		assertTrue(rows==1);
		rows = updateInboxUnreadCount(-1);
		assertTrue(rows==1);
		rows = updateSentUnreadCount(1);
		assertTrue(rows==1);
		rows = updateSentUnreadCount(-1);
		assertTrue(rows==1);
	}

	private int updateInboxUnreadCount(int delta) {
		int rowsUpdated = msgUnreadCountDao.updateInboxUnreadCount(delta);
		int count = msgUnreadCountDao.selectInboxUnreadCount();
		System.out.println("updateInboxUnreadCount() " + rowsUpdated + ", count: " + count);
		return rowsUpdated;
	}
	
	private int updateSentUnreadCount(int delta) {
		int rowsUpdated = msgUnreadCountDao.updateSentUnreadCount(delta);
		int count = msgUnreadCountDao.selectSentUnreadCount();
		System.out.println("updateSentUnreadCount() " + rowsUpdated + ", count: " + count);
		return rowsUpdated;
	}
	
	private int resetUnreadCounts() {
		msgUnreadCountDao.resetUnreadCounts(10, 5);
		int inboxCount = msgUnreadCountDao.selectInboxUnreadCount();
		int sentCount = msgUnreadCountDao.selectSentUnreadCount();
		System.out.println("updateUnreadCounts() " + inboxCount + " : " + sentCount);
		return (inboxCount + sentCount);
	}
}
