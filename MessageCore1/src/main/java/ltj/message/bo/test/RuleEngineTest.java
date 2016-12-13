package ltj.message.bo.test;

import java.io.IOException;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bean.MessageBeanUtil;
import ltj.message.bo.TaskDispatcher;
import ltj.message.bo.inbox.MessageParser;
import ltj.message.bo.inbox.MsgInboxBo;
import ltj.message.exception.DataValidationException;
import ltj.message.util.FileUtil;

public class RuleEngineTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(RuleEngineTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Resource
	MsgInboxBo msgInboxBo;
	@Resource
	private MessageParser messageParser;
	@Resource
	private TaskDispatcher taskDispatcher;
	
	final int startFrom = 1;
	final int endTo = 1;
	
	@Test
	public void processgetBouncedMails() throws IOException, MessagingException,
			DataValidationException, JMSException {
		for (int i = startFrom; i <= endTo; i++) {
			byte[] mailStream = getBouncedMail(i);
			MessageBean messageBean = MessageBeanUtil.createBeanFromStream(mailStream);
			messageBean.setIsReceived(true);
			messageParser.parse(messageBean);
			taskDispatcher.dispatchTasks(messageBean);
			// TODO verify results
		}
	}
	
	byte[] getBouncedMail(int fileNbr) {
		return FileUtil.loadFromFile("bouncedmails", "BouncedMail_" + fileNbr + ".txt");
	}
}
