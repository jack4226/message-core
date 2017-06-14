package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.constant.Constants;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.emailaddr.EmailSubscrptDao;
import ltj.message.util.EmailAddrUtil;
import ltj.message.util.PrintUtil;
import ltj.message.vo.PagingVo;
import ltj.message.vo.PagingVo.PageAction;
import ltj.message.vo.SearchSbsrVo;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.emailaddr.EmailSubscrptVo;

public class EmailSubscrptTest extends DaoTestBase {
	@Resource
	private EmailSubscrptDao emailSubscrptDao;
	@Resource
	private EmailAddressDao emailAddressDao;

	final static String listId = Constants.DEMOLIST1_NAME;
	final static String testAddr = "subtest@test.com";
	
	@Test
	public void testSelects() {
		List<EmailSubscrptVo> list = selectByListId();
		assertTrue(list.size() > 0);
		list = selectByListId2();
		assertTrue(list.size() > 0);
		list = selectByListId3();
		assertTrue(list.size() > 0);
	}

	@Test
	public void insertSelectDelete() {
		try {
			int rowsInserted = subscribe();
			assertEquals(rowsInserted, 1);
			EmailSubscrptVo vo = selectByAddrAndListId();
			assertNotNull(vo);
			assertEquals(Constants.Y, vo.getSubscribed());
			assertEquals(Constants.YES, vo.getSubscribedDesc());
			int rowsDeleted = unsubscribe();
			EmailSubscrptVo vo1 = selectByAddrAndListId();
			assertNotNull(vo1);
			assertEquals(Constants.N, vo1.getSubscribed());
			assertEquals(Constants.NO, vo1.getSubscribedDesc());
			assertEquals(1, rowsDeleted);
			rowsDeleted = delete();
			EmailSubscrptVo vo2 = selectByAddrAndListId();
			assertNull(vo2);
		}
		catch (RuntimeException e) {
			delete();
		}
	}
	
	@Test
	public void testSearchByAddr() {
		SearchSbsrVo searchVo = new SearchSbsrVo(new PagingVo());
		//PagingVo vo = searchVo.getPagingVo();
		searchVo.setListId(listId);
		
		List<EmailSubscrptVo> listAll = emailSubscrptDao.getSubscribersWithPaging(searchVo);
		assertFalse(listAll.isEmpty());
		
		searchVo.setSubscribed(true);
		List<EmailSubscrptVo> listSubed = emailSubscrptDao.getSubscribersWithPaging(searchVo);
		assertFalse(listSubed.isEmpty());
		searchVo.setSubscribed(false);
		List<EmailSubscrptVo> listUnsed = emailSubscrptDao.getSubscribersWithPaging(searchVo);
		assertEquals(listSubed.size() + listUnsed.size(), listAll.size());
		
		String emailAddr1 = listSubed.get(0).getEmailAddr();
		String emailAddr2 = listSubed.get(listSubed.size() - 1).getEmailAddr();
		
		searchVo.setSubscribed(true);
		searchVo.setEmailAddr(EmailAddrUtil.getEmailDomainName(emailAddr1) + " " + EmailAddrUtil.getEmailUserName(emailAddr2));
		List<EmailSubscrptVo> listSrched = emailSubscrptDao.getSubscribersWithPaging(searchVo);
		assertFalse(listSrched.isEmpty());
		for (EmailSubscrptVo sub : listSrched) {
			logger.info("Search result 1:" + PrintUtil.prettyPrint(sub, 2));
			assertEquals(Constants.Y, sub.getSubscribed());
		}
	}

	@Test
	public void testWithPaging() {
		int testPageSize = 4;
		SearchSbsrVo searchVo = new SearchSbsrVo(new PagingVo());
		PagingVo vo = searchVo.getPagingVo();
		searchVo.setListId(listId);
		vo.setPageSize(testPageSize);
		// fetch the first page
		List<EmailSubscrptVo> list1 = emailSubscrptDao.getSubscribersWithPaging(searchVo);
		assertFalse(list1.isEmpty());
		// fetch is again
		vo.setPageAction(PageAction.CURRENT);
		List<EmailSubscrptVo> list2 = emailSubscrptDao.getSubscribersWithPaging(searchVo);
		assertEquals(list1.size(), list2.size());
		for (int i = 0; i < list1.size(); i++) {
			assertSbsrVosAreSame(list1.get(i), list2.get(i));
		}
		// fetch the second page
		vo.setPageAction(PageAction.NEXT);
		List<EmailSubscrptVo> list3 = emailSubscrptDao.getSubscribersWithPaging(searchVo);
		if (!list3.isEmpty()) {
			assertTrue(list3.get(0).getEmailAddrId() > list1.get(list1.size() - 1).getEmailAddrId());
			// back to the first page
			vo.setPageAction(PageAction.PREVIOUS);
			List<EmailSubscrptVo> list4 = emailSubscrptDao.getSubscribersWithPaging(searchVo);
			assertEquals(list1.size(), list4.size());
			for (int i = 0; i < list1.size(); i++) {
				assertSbsrVosAreSame(list1.get(i), list4.get(i));
			}
		}
		// fetch the last page
		vo.setPageAction(PageAction.LAST);
		List<EmailSubscrptVo> list5 = emailSubscrptDao.getSubscribersWithPaging(searchVo);
		assertFalse(list5.isEmpty());
		vo.setPageAction(PageAction.PREVIOUS);
		emailSubscrptDao.getSubscribersWithPaging(searchVo);
		// back to the last page
		vo.setPageAction(PageAction.NEXT);
		List<EmailSubscrptVo> list6 = emailSubscrptDao.getSubscribersWithPaging(searchVo);
		if (emailSubscrptDao.getSubscriberCount(searchVo) <= vo.getPageSize()) {
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
		List<EmailSubscrptVo> list7 = emailSubscrptDao.getSubscribersWithPaging(searchVo);
		assertEquals(list1.size(), list7.size());
		for (int i = 0; i < list1.size(); i++) {
			assertSbsrVosAreSame(list1.get(i), list7.get(i));
		}
	}
	
	void assertSbsrVosAreSame(EmailSubscrptVo vo1, EmailSubscrptVo vo2) {
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

	private EmailSubscrptVo selectByAddrAndListId() {
		EmailSubscrptVo vo = emailSubscrptDao.getByAddrAndListId(testAddr, listId);
		if (vo != null) {
			logger.info("getByAddrAndListId(): " + vo);
		}
		return vo;
	}
	
	private int subscribe() {
		int rows = emailSubscrptDao.subscribe(testAddr, listId);
		logger.info("subscribed: " + rows);
		return rows;
	}

	private int unsubscribe() {
		int rows = emailSubscrptDao.unsubscribe(testAddr, listId);
		logger.info("unsubscribed: " + rows);
		return rows;
	}

	private int delete() {
		EmailAddressVo vo = emailAddressDao.getByAddress(testAddr);
		int rows = emailSubscrptDao.deleteByPrimaryKey(vo.getEmailAddrId(), listId);
		logger.info("delete: rows deleted: " + rows);
		return rows;
	}

	private List<EmailSubscrptVo> selectByListId() {
		emailSubscrptDao.optInConfirm("jsmith@test.com", listId);
		List<EmailSubscrptVo> list = emailSubscrptDao.getSubscribers(listId);
		if (list.isEmpty()) {
			emailSubscrptDao.subscribe("subtest@test.com", listId);
			emailSubscrptDao.subscribe("jsmith@test.com", listId);
			list = emailSubscrptDao.getSubscribers(listId);
		}
		logger.info("getSubscribers(): number of subscribers: " + list.size());
		for (int i = 0 ; i < list.size(); i++) {
			EmailSubscrptVo vo = list.get(i);
			logger.info("selectByListId() - getSubscribers(): [" + i + "] " + LF + vo);
			EmailSubscrptVo vo2;
			if (i == 0) {
				vo2 = emailSubscrptDao.getByAddrAndListId(vo.getEmailAddr(), vo.getListId());
				emailSubscrptDao.updateSentCount(vo.getEmailAddrId(), vo.getListId());
				emailSubscrptDao.updateOpenCount(vo.getEmailAddrId(), vo.getListId());
				emailSubscrptDao.updateClickCount(vo.getEmailAddrId(), vo.getListId());
				logger.info("getByAddrAndListId(): " + vo2);
				vo2 = emailSubscrptDao.getByPrimaryKey(vo.getEmailAddrId(), vo.getListId());
				logger.info("getByPrimaryKey(): " + vo2);
				List<EmailSubscrptVo>  lst2 = emailSubscrptDao.getByListId(vo.getListId());
				List<EmailSubscrptVo>  lst3 = emailSubscrptDao.getByAddrId(vo.getEmailAddrId());
				logger.info("getByListId(): " + lst2.size() + ", getByAddrId(): " + lst3.size());
			}
		}
		return list;
	}
	
	private List<EmailSubscrptVo> selectByListId2() {
		List<EmailSubscrptVo> list = emailSubscrptDao.getSubscribersWithCustomerRecord(listId);
		for (EmailSubscrptVo vo : list) {
			logger.info("getSubscribersWithCustomerRecord(): " + LF +vo);
		}
		return list;
	}
	
	private List<EmailSubscrptVo> selectByListId3() {
		List<EmailSubscrptVo> list = emailSubscrptDao.getSubscribersWithoutCustomerRecord(listId);
		for (EmailSubscrptVo vo : list) {
			logger.info("getSubscribersWithoutCustomerRecord(): " + LF + vo);
		}
		return list;
	}
}
