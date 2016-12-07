package ltj.message.main;

import static org.junit.Assert.*;

import javax.mail.Message;

import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bo.test.BoTestBase;
import ltj.message.dao.inbox.MsgStreamDao;
import ltj.vo.outbox.MsgStreamVo;

public class MsgStreamTest extends BoTestBase {

	@Test
	public void testActivate() throws Exception {
		MsgStreamDao msgStreamDao = factory.getBean(MsgStreamDao.class);
		assertNotNull(msgStreamDao);

		MsgStreamVo msgStreamVo = msgStreamDao.getLastRecord();
		assertNotNull(msgStreamVo);
		
		Message msg = createMimeMessage(msgStreamVo.getMsgStream());
		MessageBean messageBean = createMessageBean(msg);
		
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
			logger.debug("BodyContentType: " + messageBean.getBodyContentType());
			logger.debug("BodyContent: " + messageBean.getBody());
			logger.debug("BodyNode: " + LF + messageBean.getBodyNode().toString(0));
		}
	}

}
