package ltj.message.bo;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bo.mailsender.MessageBodyBuilder;
import ltj.message.constant.MailCodeType;
import ltj.message.constant.Constants;
import ltj.message.dao.idtokens.EmailIdParser;
import ltj.message.util.StringUtil;

public class MessageBodyBuilderTest {
	static final Logger logger = Logger.getLogger(MessageBodyBuilderTest.class);

	@Test
	public void testAppendTextToHtml() {
		String origText = "<HTML>This is the original message.</HTML>";
		String newText = "This is the new Text.";
		String str = MessageBodyBuilder.appendTextToHtml(origText, newText);
		logger.info("Append: "+ str);
		assertTrue(StringUtils.endsWith(str, "This is the new Text.</HTML>"));
		str = MessageBodyBuilder.prependTextToHtml(origText, newText);
		logger.info("Prepend: "+ str);
		assertTrue(StringUtils.startsWith(str, "<HTML>This is the new Text."));		
	}
	
	@Test
	public void testEmbedEmailId() {
		String origMsgId = "2458748234";
		// embed email_id for HTML email
		MessageBean msgBean = new MessageBean();
		msgBean.setEmBedEmailId(Boolean.TRUE);
		msgBean.setContentType("text/html");
		msgBean.setSubject("Test Embedding Email_Id");
		msgBean.setBody("<HTML>This is the original message." + Constants.MSG_DELIMITER_BEGIN
				+ "abcdefg" + Constants.MSG_DELIMITER_END + "</HTML>");
		msgBean.setCarrierCode(MailCodeType.SMTPMAIL.value());
		msgBean.setMsgId(Long.valueOf(origMsgId));
		msgBean.setBody(MessageBodyBuilder.getBody(msgBean));
		logger.info(">>>>>>>>>>>>>>>>HTML Message:" + StringUtil.LF + msgBean);
		
		assertEquals(origMsgId, EmailIdParser.getDefaultParser().parseMsg(msgBean.getBody()));
		assertEquals(origMsgId, EmailIdParser.getDefaultParser().parseHeaders(msgBean.getHeaders()));
		assertTrue("Email body must not contain the original msgId", !StringUtils.contains(msgBean.getBody(), origMsgId));

		origMsgId = "98764894";
		// embed email_id for plain text email
		msgBean = new MessageBean();
		msgBean.setEmBedEmailId(Boolean.TRUE);
		msgBean.setContentType("text/plain");
		msgBean.setSubject("Test Embedding Email_Id");
		msgBean.setBody("This is the original message.\n" + Constants.MSG_DELIMITER_BEGIN
				+ "abcdefg" + Constants.MSG_DELIMITER_END);
		msgBean.setCarrierCode(MailCodeType.SMTPMAIL.value());
		msgBean.setMsgId(Long.valueOf(origMsgId));
		msgBean.setBody(MessageBodyBuilder.getBody(msgBean));
		logger.info(">>>>>>>>>>>>>>>>TEXT Message:" + StringUtil.LF +msgBean);

		// parse email_id
		String msgIdFromEmailId = EmailIdParser.getDefaultParser().parseMsg(msgBean.getBody());
		logger.info("Email_Id from Body: " + msgIdFromEmailId);
		assertEquals(origMsgId, msgIdFromEmailId);
		msgIdFromEmailId = EmailIdParser.getDefaultParser().parseHeaders(msgBean.getHeaders());
		logger.info("Email_Id from X-Header: " + msgIdFromEmailId);
		assertEquals(origMsgId, msgIdFromEmailId);
	}

	@Test
	public void testNoEmailId() {
		String origMsgId = "2458748234";
		MessageBean msgBean = new MessageBean();
		msgBean.setContentType("text/html");
		msgBean.setSubject("Test Embedding Email_Id");
		msgBean.setBody("<HTML>This is the original message." + Constants.MSG_DELIMITER_BEGIN
				+ "abcdefg" + Constants.MSG_DELIMITER_END + "</HTML>");
		msgBean.setCarrierCode(MailCodeType.SMTPMAIL.value());
		msgBean.setMsgId(Long.valueOf(origMsgId));
		msgBean.setBody(MessageBodyBuilder.getBody(msgBean));
		logger.info(">>>>>>>>>>>>>>>>HTML Message:" + StringUtil.LF + msgBean);
		
		assertEquals(null, EmailIdParser.getDefaultParser().parseMsg(msgBean.getBody()));
		assertEquals(null, EmailIdParser.getDefaultParser().parseHeaders(msgBean.getHeaders()));
		assertTrue("Email body must not contain the original msgId", !StringUtils.contains(msgBean.getBody(), origMsgId));
	}
}
