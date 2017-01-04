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
	public void test1() throws Exception { // sendMail
		sendMailBo.getJmsProcessor().setQueueName("mailSenderInput");
		for (int i = 0; i < loops; i++) {
			String suffix = StringUtils.leftPad((new Random().nextInt(100) + ""), 2, "0");
			String user = "user" + suffix + "@localhost";
			suffixes[i] = suffix;
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
	}
	
	@Test
	@Rollback(value=false)
	public void test2() throws Exception { // sendMailVERP
		for (int i = loops; i < loops * 2; i++) {
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
				assertEquals(RuleNameType.SEND_MAIL.name(), vo.getRuleName());
				if ((testSubject + suffixes[i]).equals(vo.getMsgSubject())) {
					if (StringUtils.contains(vo.getToAddress(), suffixes[i])) {
						found = true;
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
				assertEquals(RuleNameType.SEND_MAIL.name(), vo.getRuleName());
				if ((testSubject + nbr).equals(vo.getMsgSubject())) {
					found = true;
				}
			}
			assertEquals(true, found);
		}
	}
}
