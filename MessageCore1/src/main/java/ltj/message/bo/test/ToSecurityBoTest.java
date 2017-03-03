package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.data.preload.RuleNameEnum;
import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskBaseBo;
import ltj.message.constant.TableColumnName;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.inbox.MsgInboxVo;

@FixMethodOrder
public class ToSecurityBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo toSecurityBo;
	
	private static String nbr = StringUtils.leftPad(new Random().nextInt(100) + "", 2, '0');
	private static String toStr = "user" + nbr + "@localhost";
	private static String testSubject = "test from toSecurityBo - " + nbr;
	private static MessageBean messageBean;
	
	@Test
	@Rollback(value=false)
	public void toSecurity1() throws Exception {
		messageBean = buildMessageBeanFromMsgStream();
		messageBean.setSubject(testSubject);
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		toSecurityBo.setTaskArguments(new String[] {"$" + TableColumnName.SECURITY_DEPT_ADDR, toStr});
		toSecurityBo.getJmsProcessor().setQueueName("mailSenderInput");
		/*
		 * this step will place a MessageBean in a queue for MailEngine to
		 * pickup, the MailEngine will then send an Forward email to the
		 * "forwardAddress", and add a record to MsgInbox table.
		 */
		Long rows = (Long) toSecurityBo.process(messageBean);
		assertNotNull(rows);
		assertTrue(rows > 0);
	}
	
	@Test
	public void toSecurity2() { // waitForMailEngine
		// wait for the MailEngine to add a record to MsgInbox
		try {
			Thread.sleep(WaitTimeInMillis);
		}
		catch (InterruptedException e) {}
	}

	@Test
	public void toSecurity3() { // verifyDatabaseRecord
		EmailAddrVo addrVo = selectEmailAddrByAddress(toStr);
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
