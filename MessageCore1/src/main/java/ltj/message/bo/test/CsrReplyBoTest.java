package ltj.message.bo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.mail.Address;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.data.preload.RuleNameEnum;
import ltj.message.bean.MessageBean;
import ltj.message.bo.task.TaskBaseBo;
import ltj.message.vo.inbox.MsgInboxWebVo;

@FixMethodOrder
public class CsrReplyBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo csrReplyBo;
	
	private final String replyBodyText = "This is Reply from CSR.";
	private static String replyToAddress = "user" + StringUtils.leftPad(new Random().nextInt(100)+"", 2, '0') + "@localhost";
	private static Long msgRefId;
	
	@Test
	@Rollback(value=false)
	public void test1() throws Exception { // csrReply
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		Address[] from = InternetAddress.parse(replyToAddress);
		messageBean.setFrom(from); // redirect to MailReader
		if (isDebugEnabled) {
			logger.debug("Original MessageBean:" + LF + messageBean);
		}
		msgRefId = messageBean.getMsgId();
		MessageBean mBean = new MessageBean();
		mBean.setOriginalMail(messageBean);
		mBean.setSubject("Re: " + messageBean.getSubject());
		mBean.setBody(replyBodyText);
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + mBean);
		}
		csrReplyBo.getJmsProcessor().setQueueName("mailSenderInput");
		/*
		 * this step will place a MessageBean in a queue for MailEngine to
		 * pickup, the MailEngine will then send an CsrReply email to the
		 * "replyToAddress", and add a record to MsgInbox table.
		 */
		csrReplyBo.process(mBean);
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
		assertTrue(list.size() > 0);
		boolean found = false;
		for (MsgInboxWebVo vo : list) {
			if (replyToAddress.equals(vo.getToAddress())) {
				if (vo.getMsgSubject().startsWith("Re:")) {
					found = true;
					//logger.info("Verify result: " + vo);
					assertEquals(RuleNameEnum.SEND_MAIL.name(), vo.getRuleName());
				}
			}
		}
		assertEquals(true, found);
	}
}
