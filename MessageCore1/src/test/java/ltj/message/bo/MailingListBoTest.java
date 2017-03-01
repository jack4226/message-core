package ltj.message.bo;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.bo.mailinglist.MailingListBo;
import ltj.message.bo.test.BoTestBase;
import ltj.message.constant.CodeType;
import ltj.message.constant.Constants;
import ltj.message.dao.emailaddr.EmailTemplateDao;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.dao.emailaddr.SubscriptionDao;
import ltj.message.exception.DataValidationException;
import ltj.message.exception.OutOfServiceException;
import ltj.message.exception.TemplateNotFoundException;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.emailaddr.EmailTemplateVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.message.vo.emailaddr.SubscriptionVo;
import ltj.message.vo.inbox.MsgInboxVo;

@FixMethodOrder
public class MailingListBoTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(MailingListBoTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	@Resource
	private MailingListBo mlServiceBo;
	@Resource
	private SubscriptionDao subsDao;
	@Resource
	private EmailTemplateDao emailTemplateDao;
	@Resource
	private MailingListDao mlDao;
	
	private static String templateId = "SampleNewsletter2";
	private static Map<String, Integer> countMap = new HashMap<>();
	
	private static String testEmailAddr = "test" + StringUtils.leftPad(new Random().nextInt(10000) + "", 4, '0') + "@test.com";
	private static String mailingListId = Constants.DEMOLIST1_NAME;
	
	@Before
	@Rollback(value=false)
	public void getBeforeCounts() {
		if (countMap.size() > 0) {
			return; 
		}
		// gather existing message counts
		EmailTemplateVo tmpltVo = emailTemplateDao.getByTemplateId(templateId);
		assertNotNull(tmpltVo);
		List<SubscriptionVo> subsList = subsDao.getByListId(tmpltVo.getListId());
		assertFalse(subsList.isEmpty());
		for (SubscriptionVo subsVo : subsList) {
			EmailAddrVo addrVo = emailAddrDao.getByAddress(subsVo.getEmailAddr());
			assertNotNull(addrVo);
			
			List<MsgInboxVo> milist = msgInboxDao.getByToAddrId(addrVo.getEmailAddrId());
			countMap.put(addrVo.getEmailAddr(), milist.size());
			logger.info("Count before: " + addrVo.getEmailAddr() + " = " + milist.size());
		}
	}
	
	@Test
	@Rollback(value=false)
	public void test1() {
		try {
			int rows = broadcast(templateId);
			assertEquals(1, rows);
			rows = sendMail(templateId);
			assertEquals(1, rows);
			rows = unSubscribe(testEmailAddr, mailingListId);
			assertTrue(rows>=0);
			rows = subscribe(testEmailAddr, mailingListId);
			assertEquals(1, rows);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	@Rollback(value=false)
	public void test2() {
		try {
			Thread.sleep(WaitTimeInMillis * 2);
		} catch (InterruptedException e) {}
	}
	
	@Test
	public void test3() { // verify results
		// verify broadcast
		EmailTemplateVo tmpltVo = emailTemplateDao.getByTemplateId(templateId);
		assertNotNull(tmpltVo);
		
		List<SubscriptionVo> subsList = subsDao.getByListId(tmpltVo.getListId());
		assertFalse(subsList.isEmpty());
		
		MailingListVo mlvo = mlDao.getByListId(tmpltVo.getListId());
		assertNotNull(mlvo);
		
		for (SubscriptionVo subsVo : subsList) {
			if (CodeType.N.value().equals(subsVo.getSubscribed())) {
				continue;
			}
			EmailAddrVo addrVo = emailAddrDao.getByAddress(subsVo.getEmailAddr());
			assertNotNull(addrVo);
			
			List<MsgInboxVo> milist = msgInboxDao.getByToAddrId(addrVo.getEmailAddrId());
			assertFalse(milist.isEmpty());
			MsgInboxVo mivo = milist.get(milist.size() - 1);
			logger.info("Count before/after: " + addrVo.getEmailAddr() + " = " + countMap.get(addrVo.getEmailAddr())
					+ "/" + milist.size());
			assertTrue(StringUtils.contains(mivo.getFromAddress(), mlvo.getAcctUserName()));
			//assertEquals(countMap.get(addrVo.getEmailAddr()), Integer.valueOf(milist.size() - 1));
			assertTrue(countMap.get(addrVo.getEmailAddr()) <= (milist.size() - 1));
		}
		
		// verify subscribe
		EmailAddrVo addrVo = emailAddrDao.getByAddress(testEmailAddr);
		assertNotNull("Email address (" + testEmailAddr + ") must be present in database.", addrVo);
		SubscriptionVo subsVo =  subsDao.getByAddrAndListId(testEmailAddr, mailingListId);
		assertNotNull("Subscription must be present in database.", subsVo);
	}
	
	private int broadcast(String templateId) {
		int mailsSent = 0;
		try {
			mailsSent = mlServiceBo.broadcast(templateId);
			logger.info("Number of emails sent: " + mailsSent);
		} catch (OutOfServiceException | TemplateNotFoundException | DataValidationException e) {
			e.printStackTrace();
			fail();
		}
		return mailsSent;
	}

	private int sendMail(String templateId) {
		String toAddr = "testto@localhost";
		Map<String, String> vars = new HashMap<String, String>();
		vars.put("CustomerName", "List Subscriber");
		int mailsSent = 0;
		try {
			mailsSent = mlServiceBo.send(toAddr, vars, templateId);
			logger.info("Number of emails sent: " + mailsSent);
		} catch (DataValidationException | TemplateNotFoundException | OutOfServiceException e) {
			e.printStackTrace();
			fail();
		}
		return mailsSent;
	}

	private int subscribe(String emailAddr, String listId) {
		int rows = 0;
		try {
			rows = mlServiceBo.subscribe(emailAddr, listId);
			logger.info("Number of rows added: " + rows);
		} catch (DataValidationException e) {
			e.printStackTrace();
			fail();
		}
		return rows;
	}

	private int unSubscribe(String emailAddr, String listId) {
		int rows = 0;
		try {
			rows = mlServiceBo.unSubscribe(emailAddr, listId);
			logger.info("Number of rows removed: " + rows);
		} catch (DataValidationException e) {
			e.printStackTrace();
			fail();
		}
		return rows;
	}
}
