package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.util.EmailAddrUtil;
import ltj.message.vo.PagingVo.PageAction;
import ltj.message.vo.PagingAddrVo;
import ltj.message.vo.emailaddr.EmailAddrVo;

public class EmailAddrTest extends DaoTestBase {
	@Resource
	private EmailAddrDao emailAddrDao;
	final String insertEmailAddr = "jdoe2@test.com"; 

	@Test
	public void insertSelectDelete() {
		try {
			EmailAddrVo vo = insert();
			assertNotNull(vo);
			EmailAddrVo vo2 = selectByAddress(vo.getEmailAddr());
			assertNotNull(vo2);
			EmailAddrVo vo3 = selectByAddrId(vo2.getEmailAddrId());
			assertNotNull(vo3);
			vo.setOrigUpdtTime(vo2.getOrigUpdtTime());
			vo.setUpdtTime(vo2.getUpdtTime());
			assertTrue(vo.equalsTo(vo2));
			int rowsUpdated = update(vo2);
			assertEquals(rowsUpdated, 4);
			rowsUpdated = emailAddrDao.updateBounceCount(vo2.getEmailAddrId(), 0);
			assertTrue(rowsUpdated > 0);
			int rowsDeleted = delete(vo);
			assertEquals(rowsDeleted, 1);
		}
		catch (Exception e) {
			EmailAddrVo vo = new EmailAddrVo();
			vo.setEmailAddr(insertEmailAddr);
			delete(vo);
		}
	}

	@Test
	public void testSearchByAddr() {
		PagingAddrVo pagingAddrVo =  new PagingAddrVo();
		pagingAddrVo.resetPageContext();
		
		List<EmailAddrVo> list1 = emailAddrDao.getEmailAddrsWithPaging(pagingAddrVo);
		assertFalse(list1.isEmpty());
		int listSize1 = list1.size();
		for (EmailAddrVo vo : list1) {
			logger.info("Search result 1 - Original email address: " + vo.getOrigEmailAddr());
		}

		pagingAddrVo.setEmailAddr("Jane  Joe  Test"); // any word search
		list1 = emailAddrDao.getEmailAddrsWithPaging(pagingAddrVo);
		assertFalse(list1.isEmpty());
		assertTrue(listSize1 >= list1.size());
		for (EmailAddrVo vo : list1) {
			assertTrue(StringUtils.containsIgnoreCase(vo.getOrigEmailAddr(), "Jane")
					|| StringUtils.containsIgnoreCase(vo.getOrigEmailAddr(), "Joe")
					|| StringUtils.containsIgnoreCase(vo.getOrigEmailAddr(), "Test"));
		}
		
		String addr1 = list1.get(0).getEmailAddr();
		String addr2 = list1.get(list1.size() - 1).getEmailAddr();
		pagingAddrVo.resetPageContext();
		pagingAddrVo.setEmailAddr(EmailAddrUtil.getEmailDomainName(addr1) + " " + EmailAddrUtil.getEmailUserName(addr2));
		List<EmailAddrVo> list2 = emailAddrDao.getEmailAddrsWithPaging(pagingAddrVo);
		assertFalse(list2.isEmpty());
		for (EmailAddrVo vo : list2) {
			logger.info("Search result 2 - Original email address: " + vo.getOrigEmailAddr());
			assertTrue(StringUtils.containsIgnoreCase(vo.getOrigEmailAddr(), EmailAddrUtil.getEmailDomainName(addr1))
					|| StringUtils.containsIgnoreCase(vo.getOrigEmailAddr(), EmailAddrUtil.getEmailUserName(addr2)));
		}
	}
	
	@Test
	public void testWithPaging() {
		int testPageSize = 4;
		PagingAddrVo vo =  new PagingAddrVo();
		vo.setPageSize(testPageSize);
		// fetch the first page
		List<EmailAddrVo> list1 = emailAddrDao.getEmailAddrsWithPaging(vo);
		assertFalse(list1.isEmpty());
		// fetch it again
		vo.setPageAction(PageAction.CURRENT);
		List<EmailAddrVo> list2 = emailAddrDao.getEmailAddrsWithPaging(vo);
		assertEquals(list1.size(), list2.size());
		for (int i = 0; i < list1.size(); i++) {
			assertEmailAddrVosAreSame(list1.get(i), list2.get(i));
		}
		// fetch the second page
		vo.setPageAction(PageAction.NEXT);
		List<EmailAddrVo> list3 = emailAddrDao.getEmailAddrsWithPaging(vo);
		if (!list3.isEmpty()) {
			assertTrue(list3.get(0).getEmailAddr().compareTo(list1.get(list1.size() - 1).getEmailAddr()) > 0);
			// back to the first page
			vo.setPageAction(PageAction.PREVIOUS);
			List<EmailAddrVo> list4 = emailAddrDao.getEmailAddrsWithPaging(vo);
			assertEquals(list1.size(), list4.size());
			for (int i = 0; i < list1.size(); i++) {
				assertEmailAddrVosAreSame(list1.get(i), list4.get(i));
			}
			// back to the second page
			vo.setPageAction(PageAction.NEXT);
			List<EmailAddrVo> list5 = emailAddrDao.getEmailAddrsWithPaging(vo);
			assertEquals(list3.size(), list5.size());
			for (int i = 0; i < list3.size(); i++) {
				assertEmailAddrVosAreSame(list3.get(i), list5.get(i));
			}
		}
		// fetch the last page
		vo.setPageAction(PageAction.LAST);
		List<EmailAddrVo> list6 = emailAddrDao.getEmailAddrsWithPaging(vo);
		assertFalse(list6.isEmpty());
		// fetch the first page again
		vo.setPageAction(PageAction.FIRST);
		List<EmailAddrVo> list7 = emailAddrDao.getEmailAddrsWithPaging(vo);
		assertEquals(list1.size(), list7.size());
		for (int i = 0; i < list1.size(); i++) {
			assertEmailAddrVosAreSame(list1.get(i), list7.get(i));
		}
	}
	
	void assertEmailAddrVosAreSame(EmailAddrVo vo1, EmailAddrVo vo2) {
		assertEquals(vo1.getAcceptHtml(), vo2.getAcceptHtml());
		assertEquals(vo1.getBounceCount(), vo2.getBounceCount());
		assertEquals(vo1.getEmailAddr(), vo2.getEmailAddr());
		assertEquals(vo1.getEmailAddrId(), vo2.getEmailAddrId());
		assertEquals(vo1.getStatusId(), vo2.getStatusId());
		assertEquals(vo1.getLastBounceTime(), vo2.getLastBounceTime());
		assertEquals(vo1.getLastSentTime(), vo2.getLastSentTime());
		assertEquals(vo1.getLastRcptTime(), vo2.getLastRcptTime());
	}
	
	private EmailAddrVo selectByAddress(String emailAddr) {
		EmailAddrVo addrVo = emailAddrDao.findByAddress(emailAddr);
		System.out.println("EmailAddrDao - selectByAddress: "+LF+addrVo);
		return addrVo;
	}
	
	private EmailAddrVo selectByAddrId(long emailAddrId) {
		EmailAddrVo emailAddr = emailAddrDao.getByAddrId(emailAddrId);
		if (emailAddr!=null) {
			System.out.println("EmailAddrDao - selectByAddrId: "+LF+emailAddr);
		}
		return emailAddr;
	}
	
	private int update(EmailAddrVo vo) {
		EmailAddrVo emailAddr = emailAddrDao.findByAddress(vo.getEmailAddr());
		int rowsUpdated = 0;
		if (emailAddr!=null) {
			emailAddr.setStatusId("A");
			emailAddr.setBounceCount(emailAddr.getBounceCount() + 1);
			rowsUpdated = emailAddrDao.update(emailAddr);
			rowsUpdated += emailAddrDao.updateLastRcptTime(emailAddr.getEmailAddrId());
			rowsUpdated += emailAddrDao.updateLastSentTime(emailAddr.getEmailAddrId());
			rowsUpdated += emailAddrDao.updateBounceCount(emailAddr);
			System.out.println("EmailAddrDao - rows updated: "+rowsUpdated);
		}
		return rowsUpdated;
	}
	
	private int delete(EmailAddrVo emailAddrVo) {
		int rowsDeleted = emailAddrDao.deleteByAddress(emailAddrVo.getEmailAddr());
		System.out.println("EmailAddrDao - delete: Rows Deleted: "+rowsDeleted);
		return rowsDeleted;
	}
	
	private EmailAddrVo insert() {
		EmailAddrVo emailAddrVo = emailAddrDao.findByAddress("jsmith@test.com");
		emailAddrVo.setEmailAddr(insertEmailAddr);
		try {
			emailAddrDao.insert(emailAddrVo);
			System.out.println("EmailAddrDao - insert: "+emailAddrVo);
		}
		catch (org.springframework.dao.DataIntegrityViolationException e) {
			System.out.println("DataIntegrityViolationException caught: " + e);
			//e.printStackTrace();
		}
		return emailAddrVo;
	}
}
