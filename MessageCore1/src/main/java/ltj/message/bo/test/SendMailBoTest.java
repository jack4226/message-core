package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskBaseBo;
import ltj.message.constant.RuleNameType;
import ltj.message.vo.inbox.MsgInboxWebVo;

@FixMethodOrder
public class SendMailBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo sendMailBo;
	
	static final int loops = 1; //Integer.MAX_VALUE;
	private static MessageBean[] messageBean = new MessageBean[loops * 2];
	private static String[] suffixes = new String[loops];
	private static String testSubject = "Test Subject - ";
	
	@Test
	@Rollback(value=false)
	public void test1() { // sendMail
		sendMailBo.getJmsProcessor().setQueueName("mailSenderInput");
		int errCount = 0;
		for (int i = 0; i < loops; i++) {
			String suffix = StringUtils.leftPad((new Random().nextInt(100) + ""), 2, "0");
			String user = "user" + suffix + "@localhost";
			suffixes[i] = suffix;
			try {
				messageBean[i] = buildMessageBeanFromMsgStream();
				messageBean[i].getHeaders().clear();
				messageBean[i].setReplyto(null);
				/* set association for verifyDatabaseRecord() */
				messageBean[i].setMsgRefId(messageBean[i].getMsgId());
				messageBean[i].setSubject(testSubject + suffix);
				messageBean[i].setBody("Test Body Message - " + suffix);
				messageBean[i].setFrom(InternetAddress.parse("testfrom@localhost", false));
				messageBean[i].setTo(InternetAddress.parse(user, false));
				if (isDebugEnabled) {
					logger.debug("MessageBean created:" + LF + messageBean[i]);
				}
				sendMailBo.process(messageBean[i]);
			}
			catch (Exception e) {
				logger.error("Exception", e);
				errCount++;
			}
		}
		assertEquals("Caught errors during testing", 0, errCount);
	}
	
	@Test
	@Rollback(value=false)
	public void test2() { // sendMailVERP
		int errCount = 0;
		for (int i = loops; i < loops * 2; i++) {
			try {
				messageBean[i] = buildMessageBeanFromMsgStream();
				messageBean[i].getHeaders().clear();
				messageBean[i].setReplyto(null);
				/* set association for verifyDatabaseRecord() */
				messageBean[i].setMsgRefId(messageBean[i].getMsgId());
				messageBean[i].setFrom(InternetAddress.parse("testto@localhost"));
				messageBean[i].setTo(InternetAddress.parse("testto@localhost"));
				// VERP test
				messageBean[i].setTo(InternetAddress.parse("testto-10.07410251.0-jsmith=test.com@localhost"));
				messageBean[i].setTo(InternetAddress.parse("testto-testlist-jsmith=test.com@localhost"));
				if (isDebugEnabled) {
					logger.debug("MessageBean created:" + LF + messageBean[i]);
				}
				sendMailBo.getJmsProcessor().setQueueName("mailSenderInput");
		
				String body = messageBean[i].getBody();
				String nbr = StringUtils.leftPad(((i - loops) + ""), 2, "0");
				messageBean[i].setSubject(testSubject + nbr);
				messageBean[i].setBody((i - loops) + LF + body);
				//messageBean.setFrom(InternetAddress.parse("test" + nbr + "@localhost"));
				sendMailBo.process(messageBean[i]);
			}
			catch (Exception e) {
				logger.error("Exception", e);
				errCount++;
			}
		}
		assertEquals("Caught errors during testing", 0, errCount);
	}
	
	@Test
	public void test3() { // waitForMailEngine
		// wait for the MailEngine to add a record to MsgInbox
		try {
			Thread.sleep(WaitTimeInMillis);
		}
		catch (InterruptedException e) {}
	}
	@Test
	public void test4() { // verifyDatabaseRecord
		for (int i = 0; i < loops; i++) {
			List<MsgInboxWebVo> list = selectMsgInboxByMsgRefId(messageBean[i].getMsgId());
			assertTrue(list.size() > 0);
			boolean found = false;
			for (MsgInboxWebVo vo : list) {
				if ((testSubject + suffixes[i]).equals(vo.getMsgSubject())) {
					if (StringUtils.contains(vo.getToAddress(), suffixes[i])) {
						found = true;
						assertEquals(RuleNameType.SEND_MAIL.name(), vo.getRuleName());
					}
				}
			}
			assertEquals(true, found);
		}
		
		for (int i = loops; i < loops * 2; i++) {
			List<MsgInboxWebVo> list = selectMsgInboxByMsgRefId(messageBean[i].getMsgId());
			assertTrue(list.size() > 0);
			String nbr = StringUtils.leftPad(((i - loops) + ""), 2, "0");
			boolean found = false;
			for (MsgInboxWebVo vo : list) {
				if ((testSubject + nbr).equals(vo.getMsgSubject())) {
					found = true;
					assertEquals(RuleNameType.SEND_MAIL.name(), vo.getRuleName());
				}
			}
			assertEquals(true, found);
		}
	}
}
