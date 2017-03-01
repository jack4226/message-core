package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.constant.Constants;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.dao.emailaddr.SubscriptionDao;
import ltj.message.util.EmailAddrUtil;
import ltj.message.util.PrintUtil;
import ltj.message.vo.PagingVo.PageAction;
import ltj.message.vo.PagingSbsrVo;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.emailaddr.SubscriptionVo;

public class SubscriptionTest extends DaoTestBase {
	final String listId = Constants.DEMOLIST1_NAME;
	final String testAddr = "subtest@test.com";
	@Resource
	private SubscriptionDao subscriptionDao;
	@Resource
	private EmailAddrDao emailAddrDao;
//	@Resource
//	private UnsubCommentsDao unsubCommentsDao;

	@Test
	public void testSelects() {
		List<SubscriptionVo> list = selectByListId();
		assertTrue(list.size()>0);
		list = selectByListId2();
		assertTrue(list.size()>0);
		list = selectByListId3();
		assertTrue(list.size()>0);
	}

	@Test
	public void insertSelectDelete() {
		try {
			int rowsInserted = subscribe();
			assertEquals(rowsInserted, 1);
			SubscriptionVo vo = selectByAddrAndListId();
			assertNotNull(vo);
			assertEquals("Y", vo.getSubscribed());
			assertEquals("Yes", vo.getSubscribedDesc());
			int rowsDeleted = unsubscribe();
			SubscriptionVo vo1 = selectByAddrAndListId();
			assertNotNull(vo1);
			assertEquals("N", vo1.getSubscribed());
			assertEquals("No", vo1.getSubscribedDesc());
			assertEquals(rowsDeleted, 1);
			rowsDeleted = delete();
			SubscriptionVo vo2 = selectByAddrAndListId();
			assertNull(vo2);
		}
		catch (RuntimeException e) {
			delete();
		}
	}
	
	@Test
	public void testSearchByAddr() {
		PagingSbsrVo vo = new PagingSbsrVo();
		vo.setListId(listId);
		
		List<SubscriptionVo> listAll = subscriptionDao.getSubscribersWithPaging(vo);
		assertFalse(listAll.isEmpty());
		
		vo.setSubscribed(true);
		List<SubscriptionVo> listSubed = subscriptionDao.getSubscribersWithPaging(vo);
		assertFalse(listSubed.isEmpty());
		vo.setSubscribed(false);
		List<SubscriptionVo> listUnsed = subscriptionDao.getSubscribersWithPaging(vo);
		assertEquals(listSubed.size() + listUnsed.size(), listAll.size());
		
		String emailAddr1 = listSubed.get(0).getEmailAddr();
		String emailAddr2 = listSubed.get(listSubed.size() - 1).getEmailAddr();
		
		vo.setSubscribed(true);
		vo.setEmailAddr(EmailAddrUtil.getEmailDomainName(emailAddr1) + " " + EmailAddrUtil.getEmailUserName(emailAddr2));
		List<SubscriptionVo> listSrched = subscriptionDao.getSubscribersWithPaging(vo);
		assertFalse(listSrched.isEmpty());
		for (SubscriptionVo sub : listSrched) {
			logger.info("Search result 1:" + PrintUtil.prettyPrint(sub, 2));
			assertEquals(Constants.Y, sub.getSubscribed());
		}
	}

	@Test
	public void testWithPaging() {
		int testPageSize = 4;
		PagingSbsrVo vo = new PagingSbsrVo();
		vo.setListId(listId);
		vo.setPageSize(testPageSize);
		// fetch the first page
		List<SubscriptionVo> list1 = subscriptionDao.getSubscribersWithPaging(vo);
		assertFalse(list1.isEmpty());
		// fetch is again
		vo.setPageAction(PageAction.CURRENT);
		List<SubscriptionVo> list2 = subscriptionDao.getSubscribersWithPaging(vo);
		assertEquals(list1.size(), list2.size());
		for (int i = 0; i < list1.size(); i++) {
			assertSbsrVosAreSame(list1.get(i), list2.get(i));
		}
		// fetch the second page
		vo.setPageAction(PageAction.NEXT);
		List<SubscriptionVo> list3 = subscriptionDao.getSubscribersWithPaging(vo);
		if (!list3.isEmpty()) {
			assertTrue(list3.get(0).getEmailAddrId() > list1.get(list1.size() - 1).getEmailAddrId());
			// back to the first page
			vo.setPageAction(PageAction.PREVIOUS);
			List<SubscriptionVo> list4 = subscriptionDao.getSubscribersWithPaging(vo);
			assertEquals(list1.size(), list4.size());
			for (int i = 0; i < list1.size(); i++) {
				assertSbsrVosAreSame(list1.get(i), list4.get(i));
			}
		}
		// fetch the last page
		vo.setPageAction(PageAction.LAST);
		List<SubscriptionVo> list5 = subscriptionDao.getSubscribersWithPaging(vo);
		assertFalse(list5.isEmpty());
		vo.setPageAction(PageAction.PREVIOUS);
		subscriptionDao.getSubscribersWithPaging(vo);
		// back to the last page
		vo.setPageAction(PageAction.NEXT);
		List<SubscriptionVo> list6 = subscriptionDao.getSubscribersWithPaging(vo);
		if (subscriptionDao.getSubscriberCount(vo) <= vo.getPageSize()) {
			assertEquals(0, list6.size());
		}
		else {
			assertEquals(list5.size(), list6.size());
			for (int i = 0; i < list5.size(); i++) {
				assertSbsrVosAreSame(list5.get(i), list6.get(i));
			}
		}
		// back to the first page
		vo.setPageAction(PageAction.FIRST);
		List<SubscriptionVo> list7 = subscriptionDao.getSubscribersWithPaging(vo);
		assertEquals(list1.size(), list7.size());
		for (int i = 0; i < list1.size(); i++) {
			assertSbsrVosAreSame(list1.get(i), list7.get(i));
		}
	}
	
	void assertSbsrVosAreSame(SubscriptionVo vo1, SubscriptionVo vo2) {
		assertEquals(vo1.getEmailAddrId(), vo2.getEmailAddrId());
		assertEquals(vo1.getListId(), vo2.getListId());
		assertEquals(vo1.getSubscribed(), vo2.getSubscribed());
		assertEquals(vo1.getSentCount(), vo2.getSentCount());
		assertEquals(vo1.getOpenCount(), vo2.getOpenCount());
		assertEquals(vo1.getClickCount(), vo2.getClickCount());
		assertEquals(vo1.getLastClickTime(), vo2.getLastClickTime());
		assertEquals(vo1.getLastOpenTime(), vo2.getLastOpenTime());
		assertEquals(vo1.getLastSentTime(), vo2.getLastSentTime());
	}

	private SubscriptionVo selectByAddrAndListId() {
		SubscriptionVo vo = subscriptionDao.getByAddrAndListId(testAddr, listId);
		if (vo != null) {
			logger.info("getByAddrAndListId(): " + vo);
		}
		return vo;
	}
	
	private int subscribe() {
		int rows = subscriptionDao.subscribe(testAddr, listId);
		logger.info("subscribed: " + rows);
		return rows;
	}

	private int unsubscribe() {
		int rows = subscriptionDao.unsubscribe(testAddr, listId);
		logger.info("unsubscribed: " + rows);
		return rows;
	}

	private int delete() {
		EmailAddrVo vo = emailAddrDao.getByAddress(testAddr);
		int rows = subscriptionDao.deleteByPrimaryKey(vo.getEmailAddrId(), listId);
		logger.info("delete: rows deleted: " + rows);
		return rows;
	}

	private List<SubscriptionVo> selectByListId() {
		subscriptionDao.optInConfirm("jsmith@test.com", listId);
		List<SubscriptionVo> list = subscriptionDao.getSubscribers(listId);
		if (list.isEmpty()) {
			subscriptionDao.subscribe("subtest@test.com", listId);
			subscriptionDao.subscribe("jsmith@test.com", listId);
			list = subscriptionDao.getSubscribers(listId);
		}
		logger.info("getSubscribers(): number of subscribers: " + list.size());
		for (int i = 0 ; i < list.size(); i++) {
			SubscriptionVo vo = list.get(i);
			logger.info("selectByListId() - getSubscribers(): [" + i + "] " + LF + vo);
			SubscriptionVo vo2;
			if (i == 0) {
				vo2 = subscriptionDao.getByAddrAndListId(vo.getEmailAddr(), vo.getListId());
				subscriptionDao.updateSentCount(vo.getEmailAddrId(), vo.getListId());
				subscriptionDao.updateOpenCount(vo.getEmailAddrId(), vo.getListId());
				subscriptionDao.updateClickCount(vo.getEmailAddrId(), vo.getListId());
				logger.info("getByAddrAndListId(): " + vo2);
				vo2 = subscriptionDao.getByPrimaryKey(vo.getEmailAddrId(), vo.getListId());
				logger.info("getByPrimaryKey(): " + vo2);
				List<SubscriptionVo>  lst2 = subscriptionDao.getByListId(vo.getListId());
				List<SubscriptionVo>  lst3 = subscriptionDao.getByAddrId(vo.getEmailAddrId());
				logger.info("getByListId(): " + lst2.size() + ", getByAddrId(): " + lst3.size());
			}
		}
		return list;
	}
	
	private List<SubscriptionVo> selectByListId2() {
		List<SubscriptionVo> list = subscriptionDao.getSubscribersWithCustomerRecord(listId);
		for (SubscriptionVo vo : list) {
			logger.info("getSubscribersWithCustomerRecord(): " + LF +vo);
		}
		return list;
	}
	
	private List<SubscriptionVo> selectByListId3() {
		List<SubscriptionVo> list = subscriptionDao.getSubscribersWithoutCustomerRecord(listId);
		for (SubscriptionVo vo : list) {
			logger.info("getSubscribersWithoutCustomerRecord(): " + LF + vo);
		}
		return list;
	}
}
