package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.emailaddr.EmailSubscrptDao;
import ltj.message.dao.emailaddr.EmailUnsubCmntDao;
import ltj.message.vo.emailaddr.EmailSubscrptVo;
import ltj.message.vo.emailaddr.EmailUnsubCmntVo;

public class EmailUnsubCmntTest extends DaoTestBase {
	@Resource
	private EmailUnsubCmntDao emailUnsubCmntDao;
	@Resource
	private EmailSubscrptDao emailSubscrptDao;

	@Test
	@Rollback(value=true)
	public void testUnsubComments() {
		List<EmailUnsubCmntVo> list = selectAll();
		if (list.size() == 0) {
			assertNotNull(insert());
			list = selectAll();
		}
		assertTrue(list.size() > 0);
		EmailUnsubCmntVo vo = selectByPrimaryKey(list.get(0).getRowId());
		assertNotNull(vo);
		EmailUnsubCmntVo vo2 = insert();
		assertNotNull(vo2);
		vo.setAddTime(vo2.getAddTime());
		vo.setRowId(vo2.getRowId());
		assertTrue(vo.equalsTo(vo2));
		int rowsUpdated = update(vo2);
		assertEquals(1, rowsUpdated);
		List<EmailUnsubCmntVo> list2 = selectByEmailAddrId(vo2.getEmailAddrId());
		assertTrue(list2.size() > list.size());
		int rowsDeleted = delete(vo2.getRowId());
		assertEquals(1, rowsDeleted);
	}
	
	private EmailUnsubCmntVo selectByPrimaryKey(int rowId) {
		EmailUnsubCmntVo unsubComments = emailUnsubCmntDao.getByPrimaryKey(rowId);
		logger.info("EmailUnsubCmntDao - selectByPrimaryKey: " + LF + unsubComments);
		return unsubComments;
	}
	
	private List<EmailUnsubCmntVo> selectAll() {
		List<EmailUnsubCmntVo> list = emailUnsubCmntDao.getFirst100();
		logger.info("EmailUnsubCmntDao - getAll() - size: " + list.size());
		list = emailUnsubCmntDao.getFirst100();
		for (EmailUnsubCmntVo vo : list) {
			logger.info("EmailUnsubCmntDao - select All: " + LF + vo);
			break;
		}
		return list;
	}
	
	private int update(EmailUnsubCmntVo vo) {
		vo.setComments(vo.getComments() + LF + " new comments.");
		int rowsUpdated = emailUnsubCmntDao.update(vo);
		logger.info("EmailUnsubCmntDao - rows updated: "+rowsUpdated);
		return rowsUpdated;
	}
	
	private int delete(int rowId) {
		int rowsDeleted = emailUnsubCmntDao.deleteByPrimaryKey(rowId);
		logger.info("EmailUnsubCmntDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private EmailUnsubCmntVo insert() {
		List<EmailUnsubCmntVo> list = emailUnsubCmntDao.getFirst100();
		if (list.size() > 0) {
			EmailUnsubCmntVo vo = list.get(list.size() - 1);
			vo.setComments("Test Comments.");
			emailUnsubCmntDao.insert(vo);
			logger.info("EmailUnsubCmntDao - insertAnother:" + LF + vo);
			return vo;
		}
		else {
			EmailSubscrptVo subvo =emailSubscrptDao.getRandomRecord();
			assertNotNull(subvo);
			EmailUnsubCmntVo vo = new EmailUnsubCmntVo();
			vo.setComments("Test Comments.");
			vo.setEmailAddrId(subvo.getEmailAddrId());
			vo.setListId(subvo.getListId());
			emailUnsubCmntDao.insert(vo);
			logger.info("EmailUnsubCmntDao - insertEmpty:"  + LF + vo);
			return vo;
		}
	}

	private List<EmailUnsubCmntVo> selectByEmailAddrId(long emailAddrId) {
		List<EmailUnsubCmntVo> list = emailUnsubCmntDao.getByEmailAddrId(emailAddrId);
		for (EmailUnsubCmntVo vo2 : list) {
			logger.info("EmailUnsubCmntDao - selectByEmailAddrId: rows returned " + list.size() + LF + vo2);
		}
		return list;
	}
	
}
