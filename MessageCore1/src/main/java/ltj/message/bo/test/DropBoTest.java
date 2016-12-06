package ltj.message.bo.test;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskBaseBo;

public class DropBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo dropBo;
	@Test
	public void drop() throws Exception {
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		dropBo.process(messageBean);
	}
}
