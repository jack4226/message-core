package ltj.message.util;

import static org.junit.Assert.*;

import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bean.MessageBeanBuilder;
import ltj.message.bean.MessageBeanUtil;

public class MessageBeanUtilTest {
	static final Logger logger = LogManager.getLogger(MessageBeanUtilTest.class);

	@Test
	public void test() {
		List<String> methodNameList = MessageBeanUtil.getMessageBeanMethodNames();
		StringBuffer sb = new StringBuffer();
		for (int i=0; i<methodNameList.size(); i++) {
			sb.append(methodNameList.get(i) + StringUtil.LF);
		}
		logger.info("Method name list:" + StringUtil.LF + sb.toString());
		assertTrue(methodNameList.contains("Body"));
		assertTrue(methodNameList.contains("From"));
		assertTrue(methodNameList.contains("To"));
		assertTrue(methodNameList.contains("FinalRcpt"));
		assertTrue(methodNameList.contains("Subject"));
		assertTrue(methodNameList.contains("ContentType"));
		
		MessageBean msgBean = new MessageBean();
		String body = "Here is the test body text.";
		msgBean.setBody(body);
		String bodyStr = MessageBeanUtil.invokeMethod(msgBean, "Body");
		logger.info("Invoke getBody(): " + bodyStr);
		int msgId = 100;
		msgBean.setMsgId(Long.valueOf(msgId));
		String msgIdStr = MessageBeanUtil.invokeMethod(msgBean, "MsgId");
		System.out.println("Invoke getMsgId(): " + msgIdStr);
		assertEquals(body, bodyStr);
		assertEquals(String.valueOf(msgId), msgIdStr);
		
		String subject = "test subject";
		msgBean.setSubject(subject);
		
		String to = "testto@test.com";
		try {
			msgBean.setTo(InternetAddress.parse(to));
		} catch (AddressException e1) {
			fail("Invalid address");
		}
		
		try {
			Address failedAddr = new InternetAddress("bad.address@localhost");
			Message msg = MessageBeanUtil.createMimeMessage(msgBean, failedAddr, "5.1.1" + StringUtil.LF + StringUtil.LF);
			Address[] addrs = msg.getAllRecipients();
			assertTrue(addrs.length == 1);
			assertEquals(to, addrs[0].toString());
			assertEquals(1, msg.getFrom().length);
			assertEquals("postmaster@localhost", msg.getFrom()[0].toString());
			
			logger.info(StringUtil.LF + "Entering MessageBeanBuilder.processPart()...");
			MessageBean mb = MessageBeanBuilder.processPart(msg, null);
			assertEquals(to, mb.getToAsString());
			assertTrue(StringUtils.startsWith(mb.getContentType(), "multipart/mixed;"));
			assertEquals(0, mb.getAttachCount());
			
			assertTrue(StringUtils.startsWith(mb.getBodyContentType(), "text/plain;"));
			assertTrue(StringUtils.startsWith(mb.getBody(), "5.1.1"));
			assertTrue(StringUtils.contains(mb.getBody(), body));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
