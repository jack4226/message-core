package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.jbatch.smtp.SmtpException;
import ltj.message.bean.MessageBean;
import ltj.message.bo.mailsender.MailSenderBoImpl;
import ltj.message.constant.RuleNameType;
import ltj.message.exception.DataValidationException;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.inbox.MsgInboxVo;

@FixMethodOrder
public class MailSenderTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(MailSenderTest.class);
	@Resource
	private MailSenderBoImpl mailSenderBo;
	
	private static List<String> suffixes = new ArrayList<>();
	private static List<String> users = new ArrayList<>();
	
	@Test
	@Rollback(value=false)
	public void test1() { // send mail
		int loops = 2;
		for (int i=0; i<loops; i++) {
			String suffix = StringUtils.leftPad(new Random().nextInt(100) + "", 2, "0");
			suffixes.add(suffix);
		}
		try {
			testSendMail(suffixes);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void test2() { // waitForMailEngine
		// wait for the MailEngine to add a record to MsgInbox
		try {
			Thread.sleep(WaitTimeInMillis);
		}
		catch (InterruptedException e) {}
	}
	
	@Test
	public void test3() { // verifyDatabaseRecord
		// now verify the database record added
		for (String addr : users) {
			EmailAddrVo addrVo = emailAddrDao.getByAddress(addr);
			assertNotNull("Address " + addr + " must have been added.", addrVo);
			List<MsgInboxVo> list = msgInboxDao.getByToAddrId(addrVo.getEmailAddrId());
			assertTrue(list.size()>0);
			for (MsgInboxVo vo : list) {
				assertEquals(RuleNameType.SEND_MAIL.name(),vo.getRuleName());
				assertTrue(vo.getMsgSubject().startsWith("Test MailSender - "));
				assertEquals("Verify result", addr, vo.getToAddress());
			}
		}
	}

	private void testSendMail(List<String> suffixes) throws DataValidationException, MessagingException, JMSException {
		long startTime = System.currentTimeMillis();
		int i;
		for (i = 0; i < suffixes.size(); i++) {
			String suffix = suffixes.get(i);
			String user = "user" + suffix + "@localhost";
			users.add(user);
			MessageBean messageBean = new MessageBean();
			messageBean.setSubject("Test MailSender - " + suffix + " " + new java.util.Date());
			messageBean.setBody("Test MailSender Body Message - " + suffix);
			messageBean.setFrom(InternetAddress.parse("testfrom@localhost", false));
			messageBean.setTo(InternetAddress.parse(user, false));
			logger.info("testSendMail() - before calling for " + user);
			try {
				mailSenderBo.process(messageBean);
			} catch (SmtpException e) {
				logger.error("Exception caught", e);
				fail();
			}
			logger.info("Email saved and sent!");
		}
		logger.info("Total Emails Queued: " + i + ", Time taken: " + (System.currentTimeMillis() - startTime) / 1000
				+ " seconds");
	}
}
