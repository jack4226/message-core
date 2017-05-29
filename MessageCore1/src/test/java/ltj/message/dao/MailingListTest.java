package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import ltj.message.constant.Constants;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.vo.emailaddr.MailingListVo;

public class MailingListTest extends DaoTestBase {
	@Resource
	private MailingListDao mailingListDao;
	
	private static String listId = Constants.DEMOLIST1_NAME;

	@Test
	public void testInsertSelectDelete() {
		try {
			MailingListVo vo = insert();
			assertNotNull(vo);
			assertTrue(vo.getListId().endsWith("_v2"));
			List<MailingListVo> list = selectByAddr(vo.getEmailAddr());
			assertTrue(list.size()>0);
			MailingListVo vo2 = selectByListId(vo);
			assertNotNull(vo2);
			vo.setCreateTime(vo2.getCreateTime());
			vo.setClickCount(vo2.getClickCount());
			vo.setOpenCount(vo2.getOpenCount());
			vo.setSentCount(vo2.getSentCount());
			vo.setUpdtTime(vo2.getUpdtTime());
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 1);
			int rowsDeleted = delete(vo);
			assertEquals(rowsDeleted, 1);
		}
		catch (RuntimeException e) {
			MailingListVo vo = new MailingListVo();
			vo.setListId(StringUtils.left(listId, 5) + "_v2");
			delete(vo);
		}
	}
	
	private MailingListVo selectByListId(MailingListVo vo) {
		MailingListVo mailingList = mailingListDao.getByListId(vo.getListId());
		if (mailingList != null) {
			logger.info("MailingListDao - selectByListId: " + LF + mailingList);
		}
		return mailingList;
	}
	
	private List<MailingListVo> selectByAddr(String emailAddr) {
		List<MailingListVo> list = mailingListDao.getByAddress(emailAddr);
		for (MailingListVo vo : list) {
			logger.info("MailingListDao - selectByAddr: " + LF + vo);
		}
		mailingListDao.getSubscribedLists(1);
		mailingListDao.getAll(false);
		mailingListDao.getAll(true);
		return list;
	}
	
	private int update(MailingListVo vo) {
		MailingListVo mailingList = mailingListDao.getByListId(vo.getListId());
		int rowsUpdated = 0;
		if (mailingList!=null) {
			mailingList.setStatusId("A");
			rowsUpdated = mailingListDao.update(mailingList);
			logger.info("MailingListDao - update: rows updated: "+rowsUpdated);
		}
		return rowsUpdated;
	}
	
	private int delete(MailingListVo mailingListVo) {
		int rowsDeleted = mailingListDao.deleteByListId(mailingListVo.getListId());
		logger.info("MailingListDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private MailingListVo insert() {
		MailingListVo mailingListVo = mailingListDao.getByListId(listId);
		mailingListVo.setListId(StringUtils.left(mailingListVo.getListId(),5)+"_v2");
		mailingListDao.insert(mailingListVo);
		logger.info("MailingListDao - insert: "+mailingListVo);
		return mailingListVo;
	}
}
