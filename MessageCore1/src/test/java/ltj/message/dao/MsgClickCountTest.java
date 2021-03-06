package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.MsgClickCountDao;
import ltj.message.util.EmailAddrUtil;
import ltj.message.util.PrintUtil;
import ltj.message.vo.PagingCountVo;
import ltj.message.vo.PagingVo.PageAction;
import ltj.message.vo.inbox.MsgClickCountVo;

public class MsgClickCountTest extends DaoTestBase {
	@Resource
	private MsgClickCountDao msgClickCountDao;

	@Test
	public void insertUpdate() {
		try {
			MsgClickCountVo vo = selectRecord();
			assertNotNull(vo);
			MsgClickCountVo vo2 = selectByPrimaryKey(vo.getMsgId());
			assertNotNull(vo2);
			vo2.setComplaintCount(vo.getComplaintCount());
			vo2.setUnsubscribeCount(vo.getUnsubscribeCount());
			assertTrue(vo.equalsTo(vo2));
			int rows = update(vo2);
			assertTrue(rows > 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testSearchByAddr() {
		PagingCountVo vo = new PagingCountVo();
		
		logger.info("MsgClickCounts Dao, search all: " + vo);
		List<MsgClickCountVo> listAll = msgClickCountDao.getBroadcastsWithPaging(vo);
		
		assertFalse(listAll.isEmpty());
		
		String addr1 = listAll.get(0).getFromAddr();
		String addr2 = listAll.get(listAll.size() - 1).getFromAddr();
		
		vo.setFromEmailAddr(EmailAddrUtil.getEmailDomainName(addr1) + " " + EmailAddrUtil.getEmailUserName(addr2));
		vo.setSentCount(listAll.get(0).getSentCount() > 0 ? 1 : 0);
		vo.setOpenCount(0);
		vo.setClickCount(0);
		List<MsgClickCountVo> listSrch  = msgClickCountDao.getBroadcastsWithPaging(vo);
		for (MsgClickCountVo count : listSrch) {
			logger.info("Search result: " + PrintUtil.prettyPrint(count, 2));
		}
	}

	@Test
	public void testWithPaging() {
		int testPageSize = 4;
		PagingCountVo vo = new PagingCountVo();
		vo.setPageSize(testPageSize);
		// fetch the first page
		List<MsgClickCountVo> list1 = msgClickCountDao.getBroadcastsWithPaging(vo);
		assertFalse(list1.isEmpty());
		// fetch is again
		vo.setPageAction(PageAction.CURRENT);
		List<MsgClickCountVo> list2 = msgClickCountDao.getBroadcastsWithPaging(vo);
		assertEquals(list1.size(), list2.size());
		for (int i = 0; i < list1.size(); i++) {
			assertClickCountsAreSame(list1.get(i), list2.get(i));
		}
		// fetch the second page
		vo.setPageAction(PageAction.NEXT);
		List<MsgClickCountVo> list3 = msgClickCountDao.getBroadcastsWithPaging(vo);
		if (!list3.isEmpty()) {
			logger.info("Found the second page, page size = " + list3.size());
			assertTrue(list3.get(0).getMsgId() < list1.get(list1.size() - 1).getMsgId());
			// back to the first back
			vo.setPageAction(PageAction.PREVIOUS);
			List<MsgClickCountVo> list4 = msgClickCountDao.getBroadcastsWithPaging(vo);
			assertEquals(list1.size(), list4.size());
			for (int i = 0; i < list1.size(); i++) {
				assertClickCountsAreSame(list1.get(i), list4.get(i));
			}
		}
		// fetch the last page
		vo.setPageAction(PageAction.LAST);
		List<MsgClickCountVo> list5 = msgClickCountDao.getBroadcastsWithPaging(vo);
		assertFalse(list5.isEmpty());
		vo.setPageAction(PageAction.PREVIOUS);
		msgClickCountDao.getBroadcastsWithPaging(vo);
		// fetch is again
		vo.setPageAction(PageAction.NEXT);
		List<MsgClickCountVo> list6 = msgClickCountDao.getBroadcastsWithPaging(vo);
		if (msgClickCountDao.getBroadcastsCount(vo) <= vo.getPageSize()) {
			assertEquals(0, list6.size());
		}
		else {
			assertEquals(list5.size(), list6.size());
			for (int i = 0; i < list5.size(); i++) {
				assertClickCountsAreSame(list5.get(i), list6.get(i));
			}
		}
		// fetch the first page again
		vo.setPageAction(PageAction.FIRST);
		List<MsgClickCountVo> list7 = msgClickCountDao.getBroadcastsWithPaging(vo);
		assertEquals(list1.size(), list7.size());
		for (int i = 0; i < list1.size(); i++) {
			assertClickCountsAreSame(list1.get(i), list7.get(i));
		}
	}

	void assertClickCountsAreSame(MsgClickCountVo vo1, MsgClickCountVo vo2) {
		assertEquals(vo1.getClickCount(), vo2.getClickCount());
		assertEquals(vo1.getMsgId(), vo2.getMsgId());
		assertEquals(vo1.getListId(), vo2.getListId());
		assertEquals(vo1.getDeliveryOption(), vo2.getDeliveryOption());
		assertEquals(vo1.getSentCount(), vo2.getSentCount());
		assertEquals(vo1.getOpenCount(), vo2.getOpenCount());
		assertEquals(vo1.getUnsubscribeCount(), vo2.getUnsubscribeCount());
		assertEquals(vo1.getComplaintCount(), vo2.getComplaintCount());
		assertEquals(vo1.getReferralCount(), vo2.getReferralCount());
		assertEquals(vo1.getLastClickTime(), vo2.getLastClickTime());
		assertEquals(vo1.getLastOpenTime(), vo2.getLastOpenTime());
	}
	
	private MsgClickCountVo selectRecord() {
		MsgClickCountVo actions = msgClickCountDao.getRandomRecord();
		if (actions != null) {
			logger.info("selectRecord - : " + LF + actions);
			return actions;
		}
		return null;
	}

	private MsgClickCountVo selectByPrimaryKey(long msgId) {
		MsgClickCountVo vo = (MsgClickCountVo) msgClickCountDao.getByPrimaryKey(msgId);
		logger.info("selectByPrimaryKey - " + LF + vo);
		return vo;
	}

	private int update(MsgClickCountVo msgClickCountVo) {
		int rows = 0;
		msgClickCountVo.setSentCount(msgClickCountVo.getSentCount() + 1);
		rows += msgClickCountDao.update(msgClickCountVo);
		rows += msgClickCountDao.updateOpenCount(msgClickCountVo.getMsgId());
		rows += msgClickCountDao.updateClickCount(msgClickCountVo.getMsgId());
		rows += msgClickCountDao.updateUnsubscribeCount(msgClickCountVo.getMsgId(), 1);
		rows += msgClickCountDao.updateComplaintCount(msgClickCountVo.getMsgId(), 1);
		rows += msgClickCountDao.updateClickCount(msgClickCountVo.getMsgId());
		logger.info("update: rows updated " + rows + LF + msgClickCountVo);
		return rows;
	}

	void deleteByPrimaryKey(MsgClickCountVo vo) {
		try {
			int rowsDeleted = msgClickCountDao.deleteByPrimaryKey(vo.getMsgId());
			logger.info("deleteByPrimaryKey: Rows Deleted: " + rowsDeleted);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	MsgClickCountVo insert() {
		MsgClickCountVo msgClickCountVo = msgClickCountDao.getRandomRecord();
		if (msgClickCountVo != null) {
			msgClickCountVo.setMsgId(msgClickCountVo.getMsgId() + 1);
			msgClickCountDao.insert(msgClickCountVo);
			logger.info("insert: " + LF + msgClickCountVo);
			return msgClickCountVo;
		}
		return null;
	}
}
