package ltj.message.dao;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.vo.PagingVo;
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
	public void testWithPaging() {
		PagingVo pagingVo =  new PagingVo();
		pagingVo.resetPageContext();
		
		List<EmailAddrVo> emailAddrList = emailAddrDao.getEmailAddrsWithPaging(pagingVo);
		assertFalse(emailAddrList.isEmpty());
		int listSize1 = emailAddrList.size();
		for (EmailAddrVo vo : emailAddrList) {
			logger.info(vo.getOrigEmailAddr());
		}

		pagingVo.setSearchString("Jane  Joe"); // any word search
		emailAddrList = emailAddrDao.getEmailAddrsWithPaging(pagingVo);
		assertFalse(emailAddrList.isEmpty());
		assertTrue(listSize1 > emailAddrList.size());
		for (EmailAddrVo vo : emailAddrList) {
			assertTrue(StringUtils.contains(vo.getOrigEmailAddr(), "Jane") || StringUtils.contains(vo.getOrigEmailAddr(), "Joe"));
		}
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
		emailAddrDao.getEmailAddrsWithPaging(new PagingVo());
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
