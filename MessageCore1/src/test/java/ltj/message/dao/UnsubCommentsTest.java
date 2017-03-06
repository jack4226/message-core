package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.emailaddr.SubscriptionDao;
import ltj.message.dao.emailaddr.UnsubCommentsDao;
import ltj.message.vo.emailaddr.SubscriptionVo;
import ltj.message.vo.emailaddr.UnsubCommentsVo;

public class UnsubCommentsTest extends DaoTestBase {
	@Resource
	private UnsubCommentsDao unsubCommentsDao;
	@Resource
	private SubscriptionDao subscriptionDao;

	@Test
	@Rollback(value=true)
	public void testUnsubComments() {
		List<UnsubCommentsVo> list = selectAll();
		if (list.size() == 0) {
			assertNotNull(insert());
			list = selectAll();
		}
		assertTrue(list.size() > 0);
		UnsubCommentsVo vo = selectByPrimaryKey(list.get(0).getRowId());
		assertNotNull(vo);
		UnsubCommentsVo vo2 = insert();
		assertNotNull(vo2);
		vo.setAddTime(vo2.getAddTime());
		vo.setRowId(vo2.getRowId());
		assertTrue(vo.equalsTo(vo2));
		int rowsUpdated = update(vo2);
		assertEquals(1, rowsUpdated);
		List<UnsubCommentsVo> list2 = selectByEmailAddrId(vo2.getEmailAddrId());
		assertTrue(list2.size() > list.size());
		int rowsDeleted = delete(vo2.getRowId());
		assertEquals(1, rowsDeleted);
	}
	
	private UnsubCommentsVo selectByPrimaryKey(int rowId) {
		UnsubCommentsVo unsubComments = unsubCommentsDao.getByPrimaryKey(rowId);
		logger.info("UnsubCommentsDao - selectByPrimaryKey: " + LF + unsubComments);
		return unsubComments;
	}
	
	private List<UnsubCommentsVo> selectAll() {
		List<UnsubCommentsVo> list = unsubCommentsDao.getFirst100();
		logger.info("UnsubCommentsDao - getAll() - size: " + list.size());
		list = unsubCommentsDao.getFirst100();
		for (UnsubCommentsVo vo : list) {
			logger.info("UnsubCommentsDao - select All: " + LF + vo);
			break;
		}
		return list;
	}
	
	private int update(UnsubCommentsVo vo) {
		vo.setComments(vo.getComments() + LF + " new comments.");
		int rowsUpdated = unsubCommentsDao.update(vo);
		logger.info("UnsubCommentsDao - rows updated: "+rowsUpdated);
		return rowsUpdated;
	}
	
	private int delete(int rowId) {
		int rowsDeleted = unsubCommentsDao.deleteByPrimaryKey(rowId);
		logger.info("UnsubCommentsDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private UnsubCommentsVo insert() {
		List<UnsubCommentsVo> list = unsubCommentsDao.getFirst100();
		if (list.size() > 0) {
			UnsubCommentsVo vo = list.get(list.size() - 1);
			vo.setComments("Test Comments.");
			unsubCommentsDao.insert(vo);
			logger.info("UnsubCommentsDao - insertAnother:" + LF + vo);
			return vo;
		}
		else {
			SubscriptionVo subvo =subscriptionDao.getRandomRecord();
			assertNotNull(subvo);
			UnsubCommentsVo vo = new UnsubCommentsVo();
			vo.setComments("Test Comments.");
			vo.setEmailAddrId(subvo.getEmailAddrId());
			vo.setListId(subvo.getListId());
			unsubCommentsDao.insert(vo);
			logger.info("UnsubCommentsDao - insertEmpty:"  + LF + vo);
			return vo;
		}
	}

	private List<UnsubCommentsVo> selectByEmailAddrId(long emailAddrId) {
		List<UnsubCommentsVo> list = unsubCommentsDao.getByEmailAddrId(emailAddrId);
		for (UnsubCommentsVo vo2 : list) {
			logger.info("UnsubCommentsDao - selectByEmailAddrId: rows returned " + list.size() + LF + vo2);
		}
		return list;
	}
	
}
