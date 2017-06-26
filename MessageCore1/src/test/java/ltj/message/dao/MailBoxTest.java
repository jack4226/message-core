package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.constant.StatusId;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.servers.MailBoxDao;
import ltj.message.vo.MailBoxVo;

public class MailBoxTest extends DaoTestBase {
	@Resource
	private MailBoxDao mailBoxDao;
	
	final static boolean OnlyActive = true;
	final static String testUserId = "TestUserId";
	
	@Test
	public void testMailbox() {
		List<MailBoxVo> list1 = mailBoxDao.getAllForTrial(OnlyActive);
		assertEquals(1, list1.size());
		for (Iterator<MailBoxVo> it = list1.iterator(); it.hasNext();) {
			MailBoxVo mailBoxVo = it.next();
			logger.info("MailBoxDao - getAllForTrial(): " + mailBoxVo);
		}
		
		List<MailBoxVo> list2 = mailBoxDao.getAll(!OnlyActive);
		assertTrue(list2.size() > 1);
		for (Iterator<MailBoxVo> it = list2.iterator(); it.hasNext();) {
			MailBoxVo mailBoxVo = it.next();
			logger.info("MailBoxDao - getAll(): " + mailBoxVo);
		}
		
		MailBoxVo vo1 = list2.get(0);
		MailBoxVo vo2 = mailBoxDao.getByPrimaryKey(vo1.getUserId(), vo1.getHostName());
		assertNotNull(vo2);
		logger.info("MailBoxDao - getByPrimaryKey: " + LF + vo2);
		
		MailBoxVo vo3 = insert(vo2, testUserId);
		assertNotNull(vo3);
		vo2.setOrigUpdtTime(vo3.getOrigUpdtTime());
		vo2.setUpdtTime(vo3.getUpdtTime());
		vo2.setRowId(vo3.getRowId());
		vo2.setUserId(vo3.getUserId());
		assertTrue(vo2.equalsTo(vo3));
		
		int rowsUpdated = update(vo3);
		assertEquals(1, rowsUpdated);
		
		int rowsDeleted = mailBoxDao.deleteByPrimaryKey(vo3.getUserId(), vo3.getHostName());
		assertEquals(1, rowsDeleted);
	}
	
	private int update(MailBoxVo mailBoxVo) {
		if (!StatusId.ACTIVE.value().equals(mailBoxVo.getStatusId())) {
			mailBoxVo.setStatusId(StatusId.ACTIVE.value());
		}
		mailBoxVo.setAllowExtraWorkers(false);
		mailBoxVo.setPurgeDupsAfter(365);
		int rows = mailBoxDao.update(mailBoxVo);
		logger.info("MailBoxDao - update: rows updated " + rows);
		return rows;
	}
	
	private MailBoxVo insert(MailBoxVo mailBoxVo, String userId) {
		mailBoxVo.setUserId(userId);
		int rows = mailBoxDao.insert(mailBoxVo);
		logger.info("MailBoxDao - insert: rows inserted " + rows + LF + mailBoxVo);
		return mailBoxVo;
	}
}
