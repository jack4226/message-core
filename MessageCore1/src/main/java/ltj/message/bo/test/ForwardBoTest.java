package ltj.message.bo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import javax.annotation.Resource;

import org.junit.FixMethodOrder;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskBaseBo;
import ltj.message.constant.EmailAddressType;
import ltj.message.constant.RuleNameType;
import ltj.message.vo.inbox.MsgInboxWebVo;

/*** Please start MailEngine and MailSender before running this test ***/
@FixMethodOrder
public class ForwardBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo forwardBo;
	
	private String forwardAddress = "testto@localhost";
	private static Long msgRefId;
	@Test
	public void test1() throws Exception { // forward
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		forwardBo.setTaskArguments("$" + EmailAddressType.FROM_ADDR + "," + forwardAddress);
		msgRefId = messageBean.getMsgId();
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		forwardBo.getJmsProcessor().setQueueName("mailSenderInput");
		/*
		 * this step will place a MessageBean in a queue for MailEngine to
		 * pickup, the MailEngine will then send an Forward email to the
		 * "forwardAddress", and add a record to MsgInbox table.
		 */
		forwardBo.process(messageBean);
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
			assertTrue(vo.getMsgSubject().startsWith("Fwd:"));
		}
	}
}
