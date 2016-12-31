package ltj.message.util;

import static org.junit.Assert.*;

import java.util.Date;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bo.test.BoTestBase;
import ltj.message.constant.RuleNameType;

public class MessageParserTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(MessageParserTest.class);

	@Test
	public void test1() {
		try {
			MessageBean mBean = new MessageBean();
			try {
				mBean.setFrom(InternetAddress.parse("event.alert@localhost", false));
				mBean.setTo(InternetAddress.parse("abc@domain.com", false));
			}
			catch (AddressException e) {
				logger.error("AddressException caught", e);
			}
			mBean.setSubject("A Exception occured");
			mBean.setValue(new Date()+ " 5.2.2 Invalid user account.");
			mBean.setMailboxUser("testUser");
			
			String ruleName = parser.parse(mBean);
			logger.info("### RuleName: " + ruleName);
			assertEquals(RuleNameType.GENERIC.name(), ruleName);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test2() {
		try {
			MessageBean mBean = new MessageBean();
			try {
				mBean.setFrom(InternetAddress.parse("postmaster@test.com", false));
				mBean.setTo(InternetAddress.parse("abc@domain.com", false));
				mBean.setFinalRcpt("finalrcpt@domain.com");
			}
			catch (AddressException e) {
				logger.error("AddressException caught", e);
			}
			mBean.setSubject("A Exception occured");
			mBean.setValue(new Date()+ " 5.2.2 Invalid user account.");
			mBean.setMailboxUser("testUser");
			
			String ruleName = parser.parse(mBean);
			logger.info("### RuleName: " + ruleName);
			assertEquals(RuleNameType.HARD_BOUNCE.name(), ruleName);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void test3() {
		try {
			MessageBean mBean = new MessageBean();
			try {
				mBean.setFrom(InternetAddress.parse("postmaster@test.com", false));
				mBean.setTo(InternetAddress.parse("abc@domain.com", false));
				mBean.setFinalRcpt(null);
			}
			catch (AddressException e) {
				logger.error("AddressException caught", e);
			}
			mBean.setSubject("A Exception occured");
			mBean.setValue(new Date()+ " 5.2.2 Invalid user account.");
			mBean.setMailboxUser("testUser");
			
			String ruleName = parser.parse(mBean);
			logger.info("### RuleName: " + ruleName);
			assertEquals("HardBounce_NoFinalRcpt", ruleName);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
