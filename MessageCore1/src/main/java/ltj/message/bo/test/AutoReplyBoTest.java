package ltj.message.bo.test;

import static org.junit.Assert.*;

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
import ltj.message.constant.Constants;
import ltj.message.vo.inbox.MsgInboxWebVo;

/*** Please start MailEngine and MailSender before running this test ***/
@FixMethodOrder
public class AutoReplyBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo autoReplyBo;
	
	private static String replyToAddress = "user" + StringUtils.leftPad(new Random().nextInt(100)+"", 2, '0') + "@localhost";
	private static MessageBean messageBean;
	
	@Test
	@Rollback(value=false)
	public void test0() {
		// work around the deadlock on inserting email address in RenderUtil
		// TODO fix the deadlock issue in AutoReplyBo/RenderUtil
		emailAddressDao.findByAddress(replyToAddress);
	}
	
	@Test
	@Rollback(value=false)
	public void test1() throws Exception { // autoReply
		messageBean = buildMessageBeanFromMsgStream();
		assertNotNull(messageBean);
		messageBean.setMailingListId(Constants.DEMOLIST1_NAME);
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
		List<MsgInboxWebVo> list = selectMsgInboxByMsgRefId(messageBean.getMsgId());
		assertTrue(list.size() > 0);
		boolean found = false;
		for (MsgInboxWebVo vo : list) {
			if (replyToAddress.equals(vo.getToAddress())) {
				if (vo.getMsgSubject().startsWith("You have subscribed to ")) {
					found = true;
					assertEquals(RuleNameEnum.SEND_MAIL.name(), vo.getRuleName());
				}
			}
		}
		assertEquals(true, found);
	}
}
