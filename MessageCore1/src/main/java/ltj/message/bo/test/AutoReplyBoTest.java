package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskBaseBo;
import ltj.message.constant.RuleNameType;
import ltj.message.vo.inbox.MsgInboxWebVo;

/*** Please start MailEngine and MailSender before running this test ***/
@FixMethodOrder
public class AutoReplyBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo autoReplyBo;
	
	private final String replyToAddress = "testto@localhost";
	private static Long msgRefId;
	
	@Test
	@Rollback(value=false)
	public void test1() throws Exception { // autoReply
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		messageBean.setMailingListId("SMPLLST1");
		Address[] from = InternetAddress.parse(replyToAddress);
		messageBean.setFrom(from); // redirect to MailReader
		autoReplyBo.setTaskArguments("SubscribeByEmailReplyHtml");
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		autoReplyBo.getJmsProcessor().setQueueName("mailSenderInput");
		/*
		 * this step will place a MessageBean in a queue for MailEngine to
		 * pickup, the MailEngine will then send an AutoReply email to the
		 * "replyToAddress", and add a record to MsgInbox table.
		 */
		autoReplyBo.process(messageBean);
		msgRefId = messageBean.getMsgId();
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
		List<MsgInboxWebVo> list = selectMsgInboxByMsgRefId(msgRefId);
		assertTrue(list.size()>0);
		for (MsgInboxWebVo vo : list) {
			assertEquals(RuleNameType.SEND_MAIL.name(),vo.getRuleName());
			assertTrue(vo.getMsgSubject().startsWith("You have subscribed to "));
		}
	}
}
