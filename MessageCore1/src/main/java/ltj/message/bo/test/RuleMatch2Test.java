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
import org.junit.Before;
import org.junit.Test;

import ltj.data.preload.RuleNameEnum;
import ltj.message.bean.BodypartBean;
import ltj.message.bean.MessageBean;
import ltj.message.bean.MessageNode;
import ltj.message.bean.MsgHeader;
import ltj.message.bo.rule.RuleLoader;
import ltj.message.bo.rule.RuleMatcher;
import ltj.message.bo.rule.RulesDataBo;
import ltj.message.constant.Constants;
import ltj.message.constant.XHeaderName;
import ltj.message.vo.rule.RuleVo;

public class RuleMatch2Test extends BoTestBase {
	static final Logger logger = LogManager.getLogger(RuleMatch2Test.class);
	@Resource
	private RulesDataBo rulesDataBo;
	@Resource
	private RuleLoader loader;
	
	private RuleMatcher matcher;
	Timestamp updtTime = new Timestamp(System.currentTimeMillis());

	@Before
	public void RuleMatcherPrepare() {
		matcher = new RuleMatcher();
	}
	@Test
	public void testRuleMatcher() {
		try {
			RuleVo ruleVo = rulesDataBo.getRuleByPrimaryKey("HardBouce_WatchedMailbox");
			System.out.println("RulesDataBoImpl - getRuleByPrimaryKey: " + LF + ruleVo);
			
			ruleVo = rulesDataBo.getRuleByPrimaryKey(RuleNameEnum.VIRUS_BLOCK.name());
			System.out.println("RulesDataBoImpl - getRuleByPrimaryKey: " + LF + ruleVo);
			
			loader.listRuleNames();
			
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
			logger.info("##### ruleName: "+ruleName+LF);

			// test #2 - PreRule: MARKETING MAIL
			mBean.setSubject("A Exception occured");
			mBean.setValue(new Date()+ "Test body message.");
			mBean.setMailboxUser("noreply");
			mBean.setReturnPath("marketing@fake.net");
			ruleName = matcher.match(mBean, loader.getPreRuleSet(), loader.getSubRuleSet());
			mBean.setReturnPath(null); // reset
			logger.info("##### ruleName: "+ruleName+LF);
			
			// test #3.1 - auto_reply
			mBean.setSubject("A Exception occured - out of the office");
			ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
			logger.info("##### ruleName: "+ruleName+LF);

			// test #3.2 - out_of_office
			mBean.setValue(new Date()+ "Test body message. will return");
			ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
			logger.info("##### ruleName: "+ruleName+LF);
			
			// test #4.1 - POSTMASTER
			mBean.setMailboxUser("testUser");
			mBean.setFrom(InternetAddress.parse("mailer-daemon@legacytojava.com", false));
			mBean.setSubject("Delivery Status Notification (Failure)");
			ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
			logger.info("##### ruleName: "+ruleName+LF);
			
			// test #4.2 - MAILBOXFULL
			mBean.setSubject("A Exception occured");
			mBean.setValue(new Date()+ "Test body message. mailbox was full");
			ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
			logger.info("##### ruleName: "+ruleName+LF);
			
			// test #4.3 - POSTMASTER
			mBean.setSubject("Returned Mail: User unknown");
			ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
			logger.info("##### ruleName: "+ruleName+LF);
			
			// test #5 - VIRUS BLOCKER
			mBean.setFrom(InternetAddress.parse("event.alert@legacytojava.com", false));
			mBean.setValue(new Date()+ "Virus abc was found..");
			mBean.setSubject("A Exception occured. norton antivirus detected");
			ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
			logger.info("##### ruleName: "+ruleName+LF);
			
			// test #6 - SPAM BLOCKER
			mBean.setSubject("A Exception occured.");
			mBean.setValue(new Date()+ "Test body message. earthlink spamblocker.");
			ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
			logger.info("##### ruleName: "+ruleName+LF);
			
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
			
			// test #8 - xheader_spam_score
			mBean.setSubject("Test X-Header rule");
			mBean.setValue(new Date()+ "Test body message.");
			header = new MsgHeader();
			header.setName("X_Spam_Score");
			header.setValue("110");
			mBean.getHeaders().clear();
			mBean.getHeaders().add(header);
			ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
			logger.info("##### ruleName: "+ruleName+LF);
			
			// test #9.1 - Post Hard Bounce
			mBean.getHeaders().clear();
			mBean.setSubject("Test Post Rule");
			mBean.setValue(new Date()+ "Post Rule test body message.");
			mBean.setRuleName(RuleNameEnum.HARD_BOUNCE.name());
			ruleName = matcher.match(mBean, loader.getPostRuleSet(), loader.getSubRuleSet());
			logger.info("##### ruleName: "+ruleName+LF);
			
			// test #9.2 - Post Rule / Sub-Rule (post_rule -> no recipient found)
			mBean.getHeaders().clear();
			mBean.setSubject("Test Post Rule No recipient found");
			mBean.setValue(new Date()+ "Test body message.");
			mBean.setTo(InternetAddress.parse("support@localhost"));
			mBean.setRuleName(RuleNameEnum.HARD_BOUNCE.name());
			ruleName = matcher.match(mBean, loader.getPostRuleSet(), loader.getSubRuleSet());
			logger.info("##### RuleName: "+ruleName+LF);
			
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
			logger.info("##### ruleName: "+ruleName+LF);
			
			// test #11 - Subscribe
			mBean.setRuleName(null);
			mBean.setTo(InternetAddress.parse(Constants.DEMOLIST1_ADDR));
			mBean.setSubject("subscribe  ");
			mBean.setValue(new Date()+ "Test body message.");
			mBean.setAttachments(null);
			ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
			logger.info("##### RuleName: "+ruleName+LF);
			
			// test #12 - UnSubscribe
			mBean.setRuleName(null);
			mBean.setTo(InternetAddress.parse(Constants.DEMOLIST1_ADDR));
			mBean.setSubject("unsubscribe");
			mBean.setValue(new Date()+ "Test body message.");
			mBean.setAttachments(null);
			ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
			logger.info("##### RuleName: "+ruleName+LF);
			
			// test #13 - Contact Us
			mBean.setRuleName(null);
			mBean.setMailboxUser("support");
			mBean.setTo(InternetAddress.parse("support@localhost"));
			mBean.setSubject("Inquiry About: Technocal Question");
			mBean.setValue(new Date()+ "Test body message.");
			mBean.setAttachments(null);
			ruleName = matcher.match(mBean, loader.getRuleSet(), loader.getSubRuleSet());
			logger.info("##### RuleName: "+ruleName+LF);
			
			// test #14
			mBean.setSubject("Test Subject");
			mBean.setAttachments(null);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}
