package ltj.message.bo.test;

import static org.junit.Assert.assertNotNull;

import javax.annotation.Resource;

import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskDispatcher;
import ltj.message.constant.RuleNameType;

public class TaskDispatcherTest extends BoTestBase {
	@Resource
	private TaskDispatcher dispr;
	
	@Test
	public void testTaskScheduler() throws Exception {
		assertNotNull(dispr);
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		messageBean.setRuleName(RuleNameType.GENERIC.toString());
		
		dispr.dispatchTasks(messageBean);
	}

}
