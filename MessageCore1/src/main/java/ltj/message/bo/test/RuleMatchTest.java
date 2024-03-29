package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import ltj.data.preload.RuleNameEnum;
import ltj.message.bean.BodypartBean;
import ltj.message.bean.MessageBean;
import ltj.message.bean.MessageNode;
import ltj.message.bean.MsgHeader;
import ltj.message.bo.rule.RuleLoader;
import ltj.message.bo.rule.RuleMatcher;
import ltj.message.constant.Constants;
import ltj.message.constant.XHeaderName;

public class RuleMatchTest extends BoTestBase {
	static final Logger logger = LogManager.getLogger(RuleMatchTest.class);
	
	Timestamp updtTime = new Timestamp(System.currentTimeMillis());
	@Resource
	private RuleLoader loader;
	
	@Test
	public void testRuleMatch() throws Exception {
		RuleMatcher matcher = new RuleMatcher();
		loader.listRuleNames();
		runTest(loader, matcher);
	}
	
	private void runTest(RuleLoader loader, RuleMatcher matcher) throws AddressException {
		MessageBean mBean = new MessageBean();
		try {
			mBean.setFrom(InternetAddress.parse("event.alert@localhost", false));
			mBean.setTo(InternetAddress.parse("watched_maibox@domain.com", false));
		}
		catch (AddressException e) {
			logger.error("AddressException caught", e);
		}
		mBean.setSubject("A Exception occured");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setMailboxUser("testUser");
		
		// test #1 - null
		String ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertNull(ruleName);

		// test #2 - PreRule: MARKETING MAIL
		mBean.setSubject("A Exception occured");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setMailboxUser("noreply");
		mBean.setReturnPath("marketing@fake.net");
		ruleName = matcher.match(mBean, loader.getPreRuleSet(), loader.getSubRuleSet());
		mBean.setReturnPath(null); // reset
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals("Unattended_Mailbox", ruleName);
		
		// test #3.1 - AUTO REPLY
		mBean.setSubject("A Exception occured - out of the office");
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals(RuleNameEnum.AUTO_REPLY.name(), ruleName);

		// test #3.2 - Out Of Office
		mBean.setSubject("Out of the office auto reply");
		mBean.setValue(new Date()+ "Test body message. will return.");
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals("OutOfOffice_AutoReply", ruleName);
		
		// test #4.1 - HARD BOUNCE (mailer-daemon)
		mBean.setMailboxUser("testUser");
		mBean.setFrom(InternetAddress.parse("mailer-daemon@localhost", false));
		mBean.setSubject("Delivery Status Notification (Failure)");
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals(RuleNameEnum.HARD_BOUNCE.name(), ruleName);
		
		// test #4.2 - MAILBOX FULL (postmaster)
		mBean.setSubject("A Exception occured");
		mBean.setValue(new Date()+ "Test body message. mailbox was full");
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		//assertEquals("MailboxFull_Body_Match", ruleName);
		assertEquals(RuleNameEnum.MAILBOX_FULL.name(), ruleName);
		
		// test #4.3 - HARD BOUNCE (postmaster)
		mBean.setSubject("Returned Mail: User unknown");
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals(RuleNameEnum.HARD_BOUNCE.name(), ruleName);
		
		// test #5 - VIRUS BLOCK
		mBean.setFrom(InternetAddress.parse("event.alert@localhost", false));
		mBean.setValue(new Date()+ "Virus abc was found.");
		mBean.setSubject("A Exception occured. norton antivirus detected");
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals(RuleNameEnum.VIRUS_BLOCK.name(), ruleName);
		
		// test #6 - SPAM BLOCK
		mBean.setSubject("A Exception occured.");
		mBean.setValue(new Date()+ "Test body message. earthlink spamblocker.");
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals(RuleNameEnum.SPAM_BLOCK.name(), ruleName);
		
		// test #7 - CHALLENGE RESPONSE
		mBean.setSubject("Re: A Exception occured.");
		MsgHeader header = new MsgHeader();
		header.setName(XHeaderName.RETURN_PATH.value());
		header.setValue("<spamblocker-challenge@bounce.earthlink.net>");
		mBean.getHeaders().add(header);
		String body = "I apologize for this automatic reply to your email." + LF + LF;
		body += "To control spam, I now allow incoming messages only from senders I have approved beforehand." + LF + LF;
		body += "If you could like to be added to my list of approved senders, please fill out the short request form (see link" + LF;
		body += "below). Once I approve you, I will receive your original message in my inbox. You do not need to resend your" + LF;
		body += "message. I apologize for this one-time inconvenience." + LF + LF;
		mBean.setValue(body);
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### ruleName: "+ruleName+LF);
		assertEquals(RuleNameEnum.CHALLENGE_RESPONSE.name(), ruleName);
		
		// test #8 - xheader_spam_score
		mBean.setSubject("Test X-Header rule");
		mBean.setValue(new Date()+ "Test body message.");
		header = new MsgHeader();
		header.setName("X_Spam_Score");
		header.setValue("110");
		mBean.getHeaders().add(header);
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals("XHeader_SpamScore", ruleName);
		
		// test #9.1 - Post Rule / Sub-Rule (post_rule -> mailbox_address)
		mBean.getHeaders().clear();
		mBean.setSubject("Test Post Rule");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setRuleName(RuleNameEnum.HARD_BOUNCE.name());
		ruleName = matcher.match(mBean, loader.getPostRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals("HardBouce_WatchedMailbox", ruleName);
		
		// test #9.2 - Post Rule / Sub-Rule (post_rule -> no recipient found)
		mBean.getHeaders().clear();
		mBean.setSubject("Test Post Rule No recipient found");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setTo(InternetAddress.parse("support@localhost"));
		mBean.setRuleName(RuleNameEnum.HARD_BOUNCE.name());
		ruleName = matcher.match(mBean, loader.getPostRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals("HardBounce_NoFinalRcpt", ruleName);
		
		// test #10 - Attachment File
		mBean.setRuleName(null);
		mBean.setSubject("your account");
		mBean.setValue(new Date()+ "Test body message.");
		MessageNode node = new MessageNode(new BodypartBean(), 1);
		BodypartBean aNode = node.getBodypartNode();
		aNode.setContentType("plain/text; name=test.bat");
		aNode.setValue("test attachment content");
		List<MessageNode> nodes = new ArrayList<MessageNode>();
		nodes.add(node);
		mBean.setAttachments(nodes);
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals("Executable_Attachment", ruleName);
		
		// test #11 - Subscribe
		mBean.setRuleName(null);
		mBean.setTo(InternetAddress.parse(Constants.DEMOLIST1_ADDR));
		mBean.setSubject("subscribe  ");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setAttachments(null);
		String ruleName_s1 = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName_s1+LF);
		
		mBean.setTo(InternetAddress.parse("demolist1@espheredemo.com"));
		mBean.setSubject("subscribe  ");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setAttachments(null);
		String ruleName_s2 = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName_s2+LF);
		assertTrue(RuleNameEnum.SUBSCRIBE.name().equals(ruleName_s1) || RuleNameEnum.SUBSCRIBE.name().equals(ruleName_s2));
		
		// test #12 - UnSubscribe
		mBean.setRuleName(null);
		mBean.setTo(InternetAddress.parse(Constants.DEMOLIST1_ADDR));
		mBean.setSubject("unsubscribe");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setAttachments(null);
		String ruleName_u1 = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName_u1+LF);

		mBean.setRuleName(null);
		mBean.setTo(InternetAddress.parse(Constants.DEMOLIST1_ADDR));
		mBean.setSubject("unsubscribe");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setAttachments(null);
		String ruleName_u2 = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName_u2+LF);
		assertTrue(RuleNameEnum.UNSUBSCRIBE.name().equals(ruleName_u1)||RuleNameEnum.UNSUBSCRIBE.name().equals(ruleName_u2));
		
		// test #13 - Contact Us
		mBean.setRuleName(null);
		mBean.setMailboxUser("support");
		mBean.setTo(InternetAddress.parse("support@localhost"));
		mBean.setSubject("Inquiry About: Technocal Question");
		mBean.setValue(new Date()+ "Test body message.");
		mBean.setAttachments(null);
		ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
		logger.info("##### RuleName: "+ruleName+LF);
		assertEquals("Contact_Us", ruleName);
		
		// test #14
		mBean.setSubject("Test Subject");
		mBean.setAttachments(null);
	}
}
