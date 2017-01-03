package ltj.message.bo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
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
	
	private static String forwardAddress = "user" + StringUtils.leftPad(new Random().nextInt(100)+"", 2, '0') + "@localhost"; //"testto@localhost";
	private static Long msgRefId;
	
	@Test
	public void test1() throws Exception { // forward
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		forwardBo.setTaskArguments(forwardAddress + ",$" + EmailAddressType.FROM_ADDR);
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
			//logger.info("Verify result: " + vo);
			assertEquals("Verify result", forwardAddress, vo.getToAddress());
		}
	}
}
