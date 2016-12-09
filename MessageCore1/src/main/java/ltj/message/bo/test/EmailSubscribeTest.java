package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.Date;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bean.SimpleEmailSender;

public class EmailSubscribeTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(EmailSubscribeTest.class);
	@Resource
	private SimpleEmailSender mSend;

	@Test
	public void testSendNotify() {
		try {
			String user = "demolist1@localhost";
			sendNotify("subscribe", "Test Subscription Body Message", user);
			sendNotify("unsubscribe", "Test Subscription Body Message", user);
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
			}
			catch (AddressException e) {
				logger.error("AddressException caught", e);
			}
			mBean.setSubject(subject);
			mBean.setValue(body + " " + new Date());
			if (mSend != null) {
				mSend.sendMessage(mBean);
			}
			else {
				logger.info("JbMain.sendNotify(): message not sent, mSend not initialized.");
			}
		}
		catch (Exception e) {
			logger.error("Exception caught during sendNotify()", e);
		}
	}
}
