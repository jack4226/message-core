package ltj.message.main;

import static org.junit.Assert.*;

import javax.annotation.Resource;
import javax.mail.Message;
import javax.mail.MessagingException;

import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bo.test.BoTestBase;
import ltj.message.dao.inbox.MsgStreamDao;
import ltj.vo.outbox.MsgStreamVo;

public class MsgStreamTest extends BoTestBase {
	@Resource
	MsgStreamDao msgStreamDao;

	@Test
	public void testActivate() {
		assertNotNull(msgStreamDao);

		MsgStreamVo msgStreamVo = msgStreamDao.getLastRecord();
		assertNotNull(msgStreamVo);
		
		Message msg;
		try {
			msg = createMimeMessage(msgStreamVo.getMsgStream());
			MessageBean messageBean = createMessageBean(msg);
			
			if (isDebugEnabled) {
				logger.debug("MessageBean created:" + LF + messageBean);
				logger.debug("BodyContentType: " + messageBean.getBodyContentType());
				logger.debug("BodyContent: " + messageBean.getBody());
				logger.debug("BodyNode: " + LF + messageBean.getBodyNode().toString(0));
			}
		}
		catch (MessagingException e) {
			e.printStackTrace();
			fail();
		}
	}

}
