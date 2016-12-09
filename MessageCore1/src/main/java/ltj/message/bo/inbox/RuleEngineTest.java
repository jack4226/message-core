package ltj.message.bo.inbox;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import ltj.jbatch.app.SpringUtil;
import ltj.message.bean.MessageBean;
import ltj.message.bean.MessageBeanUtil;
import ltj.message.bo.TaskScheduler;
import ltj.message.bo.test.BoTestBase;
import ltj.message.exception.DataValidationException;

public class RuleEngineTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(RuleEngineTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	final static String LF = System.getProperty("line.separator","\n");
	@Resource
	MsgInboxBo msgInboxBo;
	@Resource
	private MessageParser messageParser;
	final int startFrom = 1;
	final int endTo = 1;
	@BeforeClass
	public static void RuleEnginePrepare() {
		factory = SpringUtil.getAppContext();
		// TODO use annotation on factory
	}
	@Test
	public void processgetBouncedMails() throws IOException, MessagingException,
			DataValidationException, JMSException {
		for (int i = startFrom; i <= endTo; i++) {
			byte[] mailStream = getBouncedMail(i);
			MessageBean messageBean = MessageBeanUtil.createBeanFromStream(mailStream);
			messageBean.setIsReceived(true);
			messageParser.parse(messageBean);
			TaskScheduler taskScheduler = new TaskScheduler(factory);
			taskScheduler.scheduleTasks(messageBean);
		}
	}
	
	byte[] getBouncedMail(int fileNbr) throws IOException {
		InputStream is = getClass().getResourceAsStream(
				"bouncedmails/BouncedMail_" + fileNbr + ".txt");
		BufferedInputStream bis = new BufferedInputStream(is);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] bytes = new byte[512];
		int len = 0;
		try { 
			while ((len = bis.read(bytes, 0, bytes.length)) >= 0) {
				baos.write(bytes, 0, len);
			}
			byte[] mailStream = baos.toByteArray();
			baos.close();
			bis.close();
			return mailStream;
		}
		catch (IOException e) {
			throw e;
		}
	}
}
