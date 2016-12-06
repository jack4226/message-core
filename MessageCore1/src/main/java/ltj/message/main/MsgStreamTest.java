package ltj.message.main;

import javax.mail.Message;

import ltj.message.bean.MessageBean;
import ltj.message.bo.test.BoTestBase;
import ltj.message.dao.inbox.MsgStreamDao;
import ltj.vo.outbox.MsgStreamVo;

public class MsgStreamTest extends BoTestBase {

	public void testActivate() throws Exception {
		MsgStreamDao msgStreamDao = (MsgStreamDao) factory.getBean("msgStreamDao");

		long msgId = 6L;
		MsgStreamVo msgStreamVo = msgStreamDao.getByPrimaryKey(msgId);
		
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
