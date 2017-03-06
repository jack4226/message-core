package ltj.message.task;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bo.task.TaskBaseBo;
import ltj.message.bo.test.BoTestBase;

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
