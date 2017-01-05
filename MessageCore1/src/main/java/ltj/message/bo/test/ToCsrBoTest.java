package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskBaseBo;
import ltj.message.constant.RuleNameType;
import ltj.message.vo.inbox.MsgInboxWebVo;

@FixMethodOrder
public class ToCsrBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo toCsrBo;
	
	private static String nbr = StringUtils.leftPad(new Random().nextInt(100) + "", 2, '0');
	private static String toStr = "user" + nbr + "@localhost";
	private static String testSubject = "test from toCsrBo - " + nbr;
	private static MessageBean messageBean;
	
	@Test
	public void toCsr1() throws Exception {
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		toCsrBo.setTaskArguments("$CUSTOMER_CARE_INPUT");
		String jmsMsgId = (String)toCsrBo.process(messageBean);
		assertNotNull(jmsMsgId);
		assertTrue(jmsMsgId.startsWith("ID:"));
	}
	
	@Test
	public void toCsr2() throws Exception {
		messageBean = buildMessageBeanFromMsgStream();
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		messageBean.setTo(InternetAddress.parse(toStr));
		messageBean.setSubject(testSubject);
		toCsrBo.setTaskArguments("mailSenderInput");
		String jmsMsgId = (String) toCsrBo.process(messageBean);
		assertNotNull(jmsMsgId);
		assertTrue(jmsMsgId.startsWith("ID:"));
	}

	@Test
	public void toCsr3() { // waitForMailEngine
		// wait for the MailEngine to add a record to MsgInbox
		try {
			Thread.sleep(WaitTimeInMillis);
		}
		catch (InterruptedException e) {}
	}

	@Test
	public void toCsr4() { // verifyDatabaseRecord
		List<MsgInboxWebVo> list = selectMsgInboxByMsgRefId(messageBean.getMsgId());
		assertTrue(list.size() > 0);
		boolean found = false;
		for (MsgInboxWebVo vo : list) {
			if (testSubject.equals(vo.getMsgSubject())) {
				found = true;
				assertEquals(RuleNameType.SEND_MAIL.name(), vo.getRuleName());
			}
		}
		assertEquals(true, found);
	}

}
