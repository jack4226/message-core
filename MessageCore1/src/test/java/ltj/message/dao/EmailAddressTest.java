package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import ltj.message.constant.RuleCriteria;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.util.EmailAddrUtil;
import ltj.message.vo.PagingVo;
import ltj.message.vo.PagingVo.PageAction;
import ltj.message.vo.SearchAddrVo;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.inbox.MsgInboxVo;

public class EmailAddressTest extends DaoTestBase {
	@Resource
	private EmailAddressDao emailAddressDao;
	@Resource
	private MsgInboxDao msgInboxDao;
	
	final String insertEmailAddr = "jdoe2@test.com"; 

	@Test
	public void insertSelectDelete() {
		try {
			EmailAddressVo vo = insert();
			assertNotNull(vo);
			EmailAddressVo vo2 = selectByAddress(vo.getEmailAddr());
			assertNotNull(vo2);
			EmailAddressVo vo3 = selectByAddrId(vo2.getEmailAddrId());
			assertNotNull(vo3);
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			vo.setUpdtTime(vo2.getUpdtTime());
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 4);
			rowsUpdated = emailAddressDao.updateBounceCount(vo2.getEmailAddrId(), 0);
			assertTrue(rowsUpdated > 0);
			int rowsDeleted = delete(vo);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			EmailAddressVo vo = new EmailAddressVo();
			vo.setEmailAddr(insertEmailAddr);
			delete(vo);
		}
	}

	@Test
	public void testGetByMsgId() {
		MsgInboxVo mivo = msgInboxDao.getRandomRecord();
		assertNotNull(mivo);
		//if (mivo.getFromAddrId() != null) {
			EmailAddressVo fromvo = emailAddressDao.getFromByMsgId(mivo.getMsgId());
			assertNotNull(fromvo);
			assertEquals(mivo.getFromAddrId(), Long.valueOf(fromvo.getEmailAddrId()));
		//}
		//if (mivo.getToAddrId() != null) {
			EmailAddressVo tovo = emailAddressDao.getToByMsgId(mivo.getMsgId());
			assertNotNull(tovo);
			assertEquals(mivo.getToAddrId(), Long.valueOf(tovo.getEmailAddrId()));
		//}
			
		long addrid = emailAddressDao.getEmailAddrIdForPreview();
		assertTrue(addrid > 0);
		
		EmailAddressVo spvo = emailAddressDao.findByAddressSP("testsp@localhost");
		assertNotNull(spvo);
	}
	
	@Test
	public void testSearchByAddr() {
		SearchAddrVo searchVo = new SearchAddrVo(new PagingVo());
		PagingVo pagingVo = searchVo.getPagingVo();
		pagingVo.resetPageContext();
		
		List<EmailAddressVo> list1 = emailAddressDao.getEmailAddrsWithPaging(searchVo);
		assertFalse(list1.isEmpty());
		int listSize1 = list1.size();
		for (EmailAddressVo vo : list1) {
			logger.info("Search result 1 - Original email address: " + vo.getOrigEmailAddr());
		}

		// any word search
		pagingVo.setSearchCriteria(PagingVo.Column.emailAddr,
				new PagingVo.Criteria(RuleCriteria.CONTAINS, ("Jane  Joe  Test"), PagingVo.MatchBy.AnyWords));
		list1 = emailAddressDao.getEmailAddrsWithPaging(searchVo);
		assertFalse(list1.isEmpty());
		assertTrue(listSize1 >= list1.size());
		for (EmailAddressVo vo : list1) {
			assertTrue(StringUtils.containsIgnoreCase(vo.getOrigEmailAddr(), "Jane")
					|| StringUtils.containsIgnoreCase(vo.getOrigEmailAddr(), "Joe")
					|| StringUtils.containsIgnoreCase(vo.getOrigEmailAddr(), "Test"));
		}
		
		String addr1 = list1.get(0).getEmailAddr();
		String addr2 = list1.get(list1.size() - 1).getEmailAddr();
		pagingVo.resetPageContext();
		pagingVo.setSearchValue(PagingVo.Column.emailAddr,
				EmailAddrUtil.getEmailDomainName(addr1) + " " + EmailAddrUtil.getEmailUserName(addr2));
		pagingVo.getSearchCriteria(PagingVo.Column.emailAddr).setMatchBy(PagingVo.MatchBy.AnyWords);
		List<EmailAddressVo> list2 = emailAddressDao.getEmailAddrsWithPaging(searchVo);
		assertFalse(list2.isEmpty());
		for (EmailAddressVo vo : list2) {
			logger.info("Search result 2 - Original email address: " + vo.getOrigEmailAddr());
			assertTrue(StringUtils.containsIgnoreCase(vo.getOrigEmailAddr(), EmailAddrUtil.getEmailDomainName(addr1))
					|| StringUtils.containsIgnoreCase(vo.getOrigEmailAddr(), EmailAddrUtil.getEmailUserName(addr2)));
		}
		
		// all words search
		pagingVo.setSearchCriteria(PagingVo.Column.emailAddr,
				new PagingVo.Criteria(RuleCriteria.CONTAINS, ("Localhost  Test"), PagingVo.MatchBy.AllWords));
		emailAddressDao.getEmailAddrsWithPaging(searchVo).isEmpty();
	}
	
	@Test
	public void testWithPaging() {
		int testPageSize = 4;
		SearchAddrVo searchVo = new SearchAddrVo(new PagingVo());
		PagingVo vo = searchVo.getPagingVo();
		vo.setPageSize(testPageSize);
		// fetch the first page
		List<EmailAddressVo> list1 = emailAddressDao.getEmailAddrsWithPaging(searchVo);
		assertFalse(list1.isEmpty());
		// fetch it again
		vo.setPageAction(PageAction.CURRENT);
		List<EmailAddressVo> list2 = emailAddressDao.getEmailAddrsWithPaging(searchVo);
		assertEquals(list1.size(), list2.size());
		for (int i = 0; i < list1.size(); i++) {
			assertEmailAddrVosAreSame(list1.get(i), list2.get(i));
		}
		// fetch the second page
		vo.setPageAction(PageAction.NEXT);
		List<EmailAddressVo> list3 = emailAddressDao.getEmailAddrsWithPaging(searchVo);
		if (!list3.isEmpty()) {
			assertTrue(list3.get(0).getEmailAddr().compareTo(list1.get(list1.size() - 1).getEmailAddr()) > 0);
			// back to the first page
			vo.setPageAction(PageAction.PREVIOUS);
			List<EmailAddressVo> list4 = emailAddressDao.getEmailAddrsWithPaging(searchVo);
			assertEquals(list1.size(), list4.size());
			for (int i = 0; i < list1.size(); i++) {
				assertEmailAddrVosAreSame(list1.get(i), list4.get(i));
			}
			// back to the second page
			vo.setPageAction(PageAction.NEXT);
			List<EmailAddressVo> list5 = emailAddressDao.getEmailAddrsWithPaging(searchVo);
			assertEquals(list3.size(), list5.size());
			for (int i = 0; i < list3.size(); i++) {
				assertEmailAddrVosAreSame(list3.get(i), list5.get(i));
			}
		}
		// fetch the last page
		vo.setPageAction(PageAction.LAST);
		List<EmailAddressVo> list6 = emailAddressDao.getEmailAddrsWithPaging(searchVo);
		assertFalse(list6.isEmpty());
		vo.setPageAction(PageAction.PREVIOUS);
		emailAddressDao.getEmailAddrsWithPaging(searchVo);
		// back to the last page
		vo.setPageAction(PageAction.NEXT);
		List<EmailAddressVo> list7 = emailAddressDao.getEmailAddrsWithPaging(searchVo);
		if (emailAddressDao.getEmailAddressCount(searchVo) <= vo.getPageSize()) {
			assertEquals(0, list7.size());
		}
		else {
			assertEquals(list6.size(), list7.size());
			for (int i = 0; i < list6.size(); i++) {
				assertEmailAddrVosAreSame(list6.get(i), list7.get(i));
			}
		}
		// fetch the first page again
		vo.setPageAction(PageAction.FIRST);
		List<EmailAddressVo> list8 = emailAddressDao.getEmailAddrsWithPaging(searchVo);
		assertEquals(list1.size(), list8.size());
		for (int i = 0; i < list1.size(); i++) {
			assertEmailAddrVosAreSame(list1.get(i), list8.get(i));
		}
	}
	
	void assertEmailAddrVosAreSame(EmailAddressVo vo1, EmailAddressVo vo2) {
		assertEquals(vo1.getAcceptHtml(), vo2.getAcceptHtml());
		assertEquals(vo1.getBounceCount(), vo2.getBounceCount());
		assertEquals(vo1.getEmailAddr(), vo2.getEmailAddr());
		assertEquals(vo1.getEmailAddrId(), vo2.getEmailAddrId());
		assertEquals(vo1.getStatusId(), vo2.getStatusId());
		assertEquals(vo1.getLastBounceTime(), vo2.getLastBounceTime());
		assertEquals(vo1.getLastSentTime(), vo2.getLastSentTime());
		assertEquals(vo1.getLastRcptTime(), vo2.getLastRcptTime());
	}
	
	private EmailAddressVo selectByAddress(String emailAddr) {
		EmailAddressVo addrVo = emailAddressDao.findByAddress(emailAddr);
		logger.info("EmailAddressDao - selectByAddress: "+LF+addrVo);
		return addrVo;
	}
	
	private EmailAddressVo selectByAddrId(long emailAddrId) {
		EmailAddressVo emailAddr = emailAddressDao.getByAddrId(emailAddrId);
		if (emailAddr!=null) {
			logger.info("EmailAddressDao - selectByAddrId: "+LF+emailAddr);
		}
		return emailAddr;
	}
	
	private int update(EmailAddressVo vo) {
		EmailAddressVo emailAddr = emailAddressDao.findByAddress(vo.getEmailAddr());
		int rowsUpdated = 0;
		if (emailAddr!=null) {
			emailAddr.setStatusId("A");
			emailAddr.setBounceCount(emailAddr.getBounceCount() + 1);
			rowsUpdated = emailAddressDao.update(emailAddr);
			rowsUpdated += emailAddressDao.updateLastRcptTime(emailAddr.getEmailAddrId());
			rowsUpdated += emailAddressDao.updateLastSentTime(emailAddr.getEmailAddrId());
			rowsUpdated += emailAddressDao.updateBounceCount(emailAddr);
			logger.info("EmailAddressDao - rows updated: "+rowsUpdated);
		}
		return rowsUpdated;
	}
	
	private int delete(EmailAddressVo emailAddressVo) {
		int rowsDeleted = emailAddressDao.deleteByAddress(emailAddressVo.getEmailAddr());
		logger.info("EmailAddressDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private EmailAddressVo insert() {
		EmailAddressVo emailAddressVo = emailAddressDao.findByAddress("jsmith@test.com");
		emailAddressVo.setEmailAddr(insertEmailAddr);
		try {
			emailAddressDao.insert(emailAddressVo);
			logger.info("EmailAddressDao - insert: "+emailAddressVo);
		}
		catch (org.springframework.dao.DataIntegrityViolationException e) {
			logger.error("DataIntegrityViolationException caught: " + e);
			//e.printStackTrace();
		}
		return emailAddressVo;
	}
}
