package ltj.message.main;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.junit.Test;

import ltj.message.bean.BodypartUtil;
import ltj.message.bean.MessageBean;
import ltj.message.bean.MessageBeanUtil;
import ltj.message.bean.MessageNode;
import ltj.message.dao.abstrct.DaoTestBase;
import ltj.message.dao.inbox.MsgStreamDao;
import ltj.message.util.FileUtil;
import ltj.vo.outbox.MsgStreamVo;

public class MessageBeanTest extends DaoTestBase {
	static final Logger logger = Logger.getLogger(MessageBeanTest.class);
	
	@Resource
	MsgStreamDao msgStreamDao;
	
	@Test
	public void testReadFromFile() {
		try {
			String filePath = "SavedMailStreams/aim";
			MessageBean msgBean = testReadFromFile(filePath, "mail4.txt");
			msgBean.setToPlainText(true);
			List<MessageNode> mNodes = BodypartUtil.retrieveAttachments(msgBean);
			logger.info("Number of Attachments: " + mNodes.size());
			logger.info("******************************");
			logger.info("MessageBean created:" + LF + msgBean);

			assertEquals("Save $250 with DIRECTV", msgBean.getSubject());
			assertEquals("DirectStarTV <newsletter@spruceagency.com>", msgBean.getFromAsString());
			assertEquals("df153@aol.com", msgBean.getToAsString());
			assertTrue(msgBean.getBody().contains("3 Months of HBO, CINEMAX and SHOWTIME"));
			
			MessageBeanUtil.createMimeMessage(msgBean);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
	
	@Test
	public void testReadFromDB() {
		try {
			MessageBean msgBean = testReadFromDatabase();
			msgBean.setToPlainText(true);
			List<MessageNode> mNodes = BodypartUtil.retrieveAttachments(msgBean);
			logger.info("Number of Attachments: " + mNodes.size());
			logger.info("******************************");
			logger.info("MessageBean created:" + LF + msgBean);

			MessageBeanUtil.createMimeMessage(msgBean);
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
	
	private MessageBean testReadFromFile(String filePath, String fileName) throws MessagingException {
		byte[] mailStream = FileUtil.loadFromFile(filePath, fileName);
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}	
	
	private MessageBean testReadFromDatabase() throws MessagingException {
		MsgStreamVo msgStreamVo = msgStreamDao.getLastRecord();
		assertNotNull(msgStreamVo);
		logger.info("MsgStreamDao - getLastRecord: "+LF+msgStreamVo);
		byte[] mailStream = msgStreamVo.getMsgStream();
		MessageBean msgBean = MessageBeanUtil.createBeanFromStream(mailStream);
		return msgBean;
	}
}
