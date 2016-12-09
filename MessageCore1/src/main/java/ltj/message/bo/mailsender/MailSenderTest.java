package ltj.message.bo.mailsender;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import ltj.jbatch.queue.JmsProcessor;
import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskBaseBo;
import ltj.message.bo.TaskScheduler;
import ltj.message.bo.test.BoTestBase;
import ltj.message.exception.DataValidationException;

public class MailSenderTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(MailSenderTest.class);
	@Resource
	private TaskBaseBo sendMailBo;
	@Autowired
	private static JmsProcessor jmsProcessor;
	@BeforeClass
	public static void MailSenderPrepare() { // TODO fix this
		jmsProcessor = TaskScheduler.getMailSenderFactory().getBean(JmsProcessor.class);
	}
	@Test
	public void testMailSender() {
		int loops = 2; //Integer.MAX_VALUE;
		try {
			testSendMail(loops);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	private void testSendMail(int loops) throws DataValidationException, MessagingException,
			JMSException, IOException {
		sendMailBo.setJmsProcessor(jmsProcessor);
		long startTime = new java.util.Date().getTime();
		int i;
		for (i = 0; i < loops; i++) {
			String suffix = StringUtils.leftPad((i % 100) + "", 2, "0");
			String user = "user" + suffix + "@localhost";
			if (i % 13 == 0) {
				try {
					Thread.sleep(2 * 1000);
				}
				catch (InterruptedException e) {
					break;
				}
			}
			MessageBean messageBean = new MessageBean();
			messageBean.setSubject("Test MailSender - " + suffix + " " + new java.util.Date());
			messageBean.setBody("Test MailSender Body Message - " + suffix);
			messageBean.setFrom(InternetAddress.parse("testfrom@localhost", false));
			messageBean.setTo(InternetAddress.parse(user, false));
			logger.info("testSendMail() - before calling for " + user);
			Long mailsSent = (Long) sendMailBo.process(messageBean);
			logger.info("Emails Queued: " + mailsSent);
		}
		logger.info("Total Emails Queued: " + i + ", Time taken: "
				+ (new java.util.Date().getTime() - startTime) / 1000 + " seconds");
	}
}
