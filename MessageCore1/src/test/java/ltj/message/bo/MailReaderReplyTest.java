package ltj.message.bo;

import static org.junit.Assert.*;

import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import ltj.message.bean.BodypartUtil;
import ltj.message.bean.MessageBean;
import ltj.message.bean.MessageBeanBuilder;
import ltj.message.bean.MessageBeanUtil;
import ltj.message.bean.MessageNode;
import ltj.message.bo.mailreader.MailReaderReply;
import ltj.message.bo.test.BoTestBase;
import ltj.message.util.FileUtil;

public class MailReaderReplyTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(MailReaderReplyTest.class);
	
	private static String body;
	private static String contentType;
	
	@Test
	public void testMailReply() {
		try {
			Message msg = buildMessage();
			
			assertNotNull(msg);
			assertNotNull(body);
			assertNotNull(contentType);
			
			Message reply = new MailReaderReply().composeReply(msg, body, contentType);
			
			assertNotNull(reply);
			
			MessageBean msgBean = MessageBeanBuilder.processPart(reply, null);
			
			assertNotNull(msgBean);
			
			logger.info("Reply Message - MessageBean:" + LF + msgBean);
			
			assertEquals("Delivery Failure: Message exceeds local size limit.", msgBean.getSubject());
			assertTrue(StringUtils.contains(msgBean.toString(0), "We're sorry, but the size of the email body you sent is too large"));
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	private Message buildMessage() throws Exception {
		try {
			String filePath = "SavedMailStreams/aim";
			MessageBean msgBean = testReadFromFile(filePath, "mail6.txt");
			msgBean.setToPlainText(true);
			List<MessageNode> mNodes = BodypartUtil.retrieveAttachments(msgBean);
			logger.info("Number of Attachments: " + mNodes.size());
			logger.info("******************************");
			logger.info("MessageBean created:" + LF + msgBean);
			
			body = msgBean.getBody();
			contentType = msgBean.getContentType();
			
			Message msg = MessageBeanUtil.createMimeMessage(msgBean);
			return msg;
		}
		catch (Exception e) {
			throw e;
		}
	}
	
	private MessageBean testReadFromFile(String filePath, String fileName) throws MessagingException {
		byte[] mailStream = FileUtil.loadFromFile(filePath, fileName);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}	
	
}
