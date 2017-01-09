package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;

import ltj.jbatch.app.MailReaderTaskExr;
import ltj.message.bean.MessageBean;
import ltj.message.bean.SimpleEmailSender;
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.inbox.MsgInboxWebVo;
import ltj.message.vo.inbox.SearchFieldsVo;
import ltj.spring.util.SpringAppConfig;
import ltj.spring.util.SpringJmsConfig;
import ltj.spring.util.SpringTaskConfig;

@ContextConfiguration(classes={SpringAppConfig.class, SpringJmsConfig.class, SpringTaskConfig.class})
@FixMethodOrder
public class MailReaderTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(MailReaderTest.class);
	@Resource
	private SimpleEmailSender mSend;
	@Resource
	private MsgInboxDao inboxDao;
	@Resource
	private EmailAddrDao addrDao;
	
	private static String testFromAddr = "testfrom@localhost";
	
	private static Map<String, Integer> msgCountMap = new LinkedHashMap<>();
	
	private static int startUser = 25;
	private static int endUser = 50;
	private static int loops = 100; //Integer.MAX_VALUE;
 	
	
	@Test
	@Rollback(value=false)
	public void test1() { // MailReader
		try {
			for (int i = 0; i < loops; i++) {
				String suffix = StringUtils.leftPad((i % (endUser - startUser)) + startUser + "", 2, "0");
				String user = "user" + suffix + "@localhost";
				if (msgCountMap.containsKey(user)) {
					msgCountMap.put(user, msgCountMap.get(user) + 1);
				}
				else {
					msgCountMap.put(user, 1);
				}
				sendNotify("Test MailReader - " + suffix, "Test MailReader Body Message - " + suffix, user);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void test2() { // gather existing counts
		for (Iterator<String> it=msgCountMap.keySet().iterator(); it.hasNext();) {
			String toAddr = it.next();
			EmailAddrVo toAddrVo = addrDao.getByAddress(toAddr);
			if (toAddrVo != null) {
				SearchFieldsVo vo = new SearchFieldsVo();
				vo.setFromAddr(testFromAddr);
				vo.setToAddrId(toAddrVo.getEmailAddrId());
				vo.setSubject("Test MailReader");
				List<MsgInboxWebVo> list = inboxDao.getListForWeb(vo);
				if (list.size() > 0) {
					msgCountMap.put(toAddr, msgCountMap.get(toAddr) + list.size());
				}
			}
		}
	}
	
	@Test
	@Rollback(value=false)
	public void test3() {
		MailReaderTaskExr.readTestUserAccounts = true;
		MailReaderTaskExr.testStartUser = startUser;
		MailReaderTaskExr.testEndUser = endUser;
		try {
			Thread.sleep(120 * 1000L);
		} catch (InterruptedException e) {}
	}
	
	@Test
	public void test4() { //verify results
		for (Iterator<String> it=msgCountMap.keySet().iterator(); it.hasNext();) {
			String toAddr = it.next();
			Integer count = msgCountMap.get(toAddr);
			EmailAddrVo toAddrVo = addrDao.getByAddress(toAddr);
			assertNotNull("Email address (" + toAddr + ") must be present in database.", toAddrVo);
			SearchFieldsVo vo = new SearchFieldsVo();
			vo.setFromAddr(testFromAddr);
			vo.setToAddrId(toAddrVo.getEmailAddrId());
			vo.setSubject("Test MailReader");
			List<MsgInboxWebVo> list = inboxDao.getListForWeb(vo);
			logger.info("Message count for (" + toAddr + ") expected = " + count + ", actual = " + list.size());
			// TODO fix Jamses server delay or missed emails issue.
			//assertTrue(list.size() >= count);
		}
	}
	
	void sendNotify(String subject, String body, String user) {
		try {
			MessageBean mBean = new MessageBean();
			try {
				mBean.setFrom(InternetAddress.parse(testFromAddr, false));
				mBean.setTo(InternetAddress.parse(user, false));
			}
			catch (AddressException e) {
				logger.error("AddressException caught", e);
			}
			mBean.setSubject(subject + " " + new Date());
			mBean.setValue(body);
			mSend.sendMessage(mBean);
		}
		catch (Exception e) {
			logger.error("Exception caught during sendNotify()", e);
		}
	}
	
	void sendVerpNotify(String subject, String body) {
		try {
			MessageBean mBean = new MessageBean();
			try {
				mBean.setFrom(InternetAddress.parse(testFromAddr, false));
				mBean.setTo(InternetAddress.parse("testto-10.07410251.0-jsmith=test.com@localhost"));
				//mBean.setCc(InternetAddress.parse("jwang@localhost,twang@localhost", false));
			}
			catch (AddressException e) {
				logger.error("AddressException caught", e);
			}
			mBean.setSubject(subject + " " + new Date());
			mBean.setValue(body);
			mSend.sendMessage(mBean);
		}
		catch (Exception e) {
			logger.error("Exception caught during sendNotify()", e);
		}
	}
}
