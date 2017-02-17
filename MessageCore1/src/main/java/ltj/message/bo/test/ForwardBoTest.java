package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.data.preload.RuleNameEnum;
import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskBaseBo;
import ltj.message.constant.AddressType;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.inbox.MsgInboxVo;

/*** Please start MailEngine and MailSender before running this test ***/
@FixMethodOrder
public class ForwardBoTest extends BoTestBase {
	protected static final Logger logger = Logger.getLogger(ForwardBoTest.class);
	@Resource
	private TaskBaseBo forwardBo;
	
	private static String forwardAddress = "user" + StringUtils.leftPad(new Random().nextInt(100)+"", 2, '0') + "@localhost"; //"testto@localhost";
	private static MessageBean messageBean;
	
	@Test
	@Rollback(value=false)
	public void test1() throws Exception { // forward
		messageBean = buildMessageBeanFromMsgStream();
		forwardBo.setTaskArguments("$" + AddressType.FROM_ADDR.value());
		forwardBo.setTaskArguments(forwardAddress);
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
		EmailAddrVo addrVo = selectEmailAddrByAddress(forwardAddress);
		assertNotNull(addrVo);
		List<MsgInboxVo> msgList = msgInboxDao.getByToAddrId(addrVo.getEmailAddrId());
		assertFalse(msgList.isEmpty());
		boolean found = false;
		for (MsgInboxVo vo : msgList) {
			if (vo.getMsgSubject().startsWith("Fwd:")) {
				if (StringUtils.contains(vo.getMsgSubject(), messageBean.getSubject())) {
					found = true;
					assertEquals(RuleNameEnum.SEND_MAIL.name(), vo.getRuleName());
				}
			}
		}
		assertEquals(true, found);
	}
}
