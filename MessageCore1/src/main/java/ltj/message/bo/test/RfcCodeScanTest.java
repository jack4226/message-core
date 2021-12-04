package ltj.message.bo.test;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import ltj.data.preload.RuleNameEnum;
import ltj.message.bo.inbox.RfcCodeScan;

public class RfcCodeScanTest extends BoTestBase {
	static final Logger logger = LogManager.getLogger(RfcCodeScanTest.class);
	
	@Test
	public void testSoftBounce() throws Exception {
		RfcCodeScan scanner = RfcCodeScan.getInstance();
		
		String bodyText = "test message 421 Service not available, closing transmission channel";
		String ruleName = scanner.examineBody(bodyText);
		logger.info("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameEnum.SOFT_BOUNCE.name().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 422 Service not available, user mailbox full";
		ruleName = scanner.examineBody(bodyText);
		logger.info("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameEnum.MAILBOX_FULL.name().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 450 Requested mail action not taken: mailbox unavailable";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameEnum.SOFT_BOUNCE.name().equals(ruleName));
		logger.info(LF);

		bodyText = "test message 451 Requested action aborted: local error in processing";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameEnum.SOFT_BOUNCE.name().equals(ruleName));
		logger.info(LF);

		bodyText = "test message 452 Requested action not taken: insufficient system storage";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameEnum.SOFT_BOUNCE.name().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 455 delivery temporary failure";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameEnum.SOFT_BOUNCE.name().equals(ruleName));
		logger.info(LF);
	}
	@Test
	public void testHardBounce() throws Exception {
		RfcCodeScan scanner = RfcCodeScan.getInstance();
		
		String bodyText = "test message 500 Syntax error, command unrecognized";
		String ruleName = scanner.examineBody(bodyText);
		logger.info("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameEnum.HARD_BOUNCE.name().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 501 Syntax error in parameters or arguments";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameEnum.HARD_BOUNCE.name().equals(ruleName));
		logger.info(LF);

		bodyText = "test message 502 Command not implemented";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameEnum.HARD_BOUNCE.name().equals(ruleName));
		logger.info(LF);

		bodyText = "test message 503 Bad sequence of commands";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameEnum.HARD_BOUNCE.name().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 504 Command parameter not implemented";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameEnum.HARD_BOUNCE.name().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 550 Requested action not taken: mailbox unavailable";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameEnum.HARD_BOUNCE.name().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 551 User not local; please try <forward-path>";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameEnum.HARD_BOUNCE.name().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 552 Requested mail action aborted: exceeded storage allocation";
		ruleName = scanner.examineBody(bodyText);
		logger.info("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameEnum.MAILBOX_FULL.name().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 553 Requested action not taken: mailbox name not allowed";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameEnum.HARD_BOUNCE.name().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 554 Transaction failed";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameEnum.HARD_BOUNCE.name().equals(ruleName));
		logger.info(LF);
		
		bodyText = "test message 555 delivery permanent error";
		ruleName = scanner.examineBody(bodyText);
		System.out.println("BodyText: " + bodyText + ", RuleName: " + ruleName);
		assertTrue(RuleNameEnum.HARD_BOUNCE.name().equals(ruleName));
		logger.info(LF);
	}
}
