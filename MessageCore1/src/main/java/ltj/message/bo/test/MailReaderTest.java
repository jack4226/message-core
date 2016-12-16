package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.Date;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bean.SimpleEmailSender;

public class MailReaderTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(MailReaderTest.class);
	@Resource
	private SimpleEmailSender mSend;
	
	@Test
	public void testMailReader() {
		try {
			int loops = 1; //Integer.MAX_VALUE;
			for (int i = 0; i < loops; i++) {
				String suffix = StringUtils.leftPad((i % 100) + "", 2, "0");
				String user = "user" + suffix + "@localhost";
				if (i % 13 == 0) {
					try {
						Thread.sleep(1 * 1000);
					}
					catch (InterruptedException e) {
						break;
					}
				}
				sendNotify("Test MailReader - " + suffix, "Test MailReader Body Message - " + suffix, user);
			}
			// TODO verify results
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	void sendNotify(String subject, String body, String user) {
		try {
			MessageBean mBean = new MessageBean();
			try {
				mBean.setFrom(InternetAddress.parse("testfrom@localhost", false));
				mBean.setTo(InternetAddress.parse(user, false));
				mBean.setTo(InternetAddress.parse("testto-10.07410251.0-jsmith=test.com@localhost"));
				//mBean.setCc(InternetAddress.parse("jwang@localhost,twang@localhost", false));
			}
			catch (AddressException e) {
				logger.error("AddressException caught", e);
			}
			mBean.setSubject(subject + " " + new Date());
			mBean.setValue(body);
			mSend.sendMessage(mBean);
		}
		catch (Exception e) {
			logger.error("Exception caught during sendNotify()", e);
		}
	}
}
