package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.Random;

import javax.annotation.Resource;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskBaseBo;
import ltj.message.constant.RuleNameType;
import ltj.message.vo.inbox.MsgInboxVo;

/*** Please deploy MailEngine and start JBoss before running this test ***/
@FixMethodOrder
public class AssignRuleNameBoTest extends BoTestBase {
	@Resource
	private TaskBaseBo assignRuleNameBo;
	private static RuleNameType testRuleName;
	private static Long msgId;
	
	@BeforeClass
	public static void prepare() {
		Random random = new Random();
		testRuleName = RuleNames[random.nextInt(RuleNames.length - 1)];
	}
	
	@Test
	@Rollback(false)
	public void test1() throws Exception { //assignRuleName
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		assignRuleNameBo.setTaskArguments(testRuleName.name());
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		/*
		 * this step will place a MessageBean with the Assigned RuleName in a
		 * queue for MailEngine to pickup, the MailEngine will then process the
		 * MessageBean and add a record to MsgInbox table.
		 */
		assignRuleNameBo.process(messageBean);
		msgId = messageBean.getMsgId();
	}
	@Test
	public void test2() { // waitForMailEngine
		// wait for the MailEngine to add a record with Assigned RuleName to MsgInbox
		try {
			Thread.sleep(5 * 1000);
		}
		catch (InterruptedException e) {}
	}
	@Test
	public void test3() { // verifyDatabaseRecord
		// now verify the database record added
		MsgInboxVo vo1 = selectMsgInboxByMsgId(msgId);
		assertNotNull(vo1);
		MsgInboxVo vo2 = selectLastReceivedMessage();
		assertTrue(testRuleName.name().equals(vo2.getRuleName()));
	}
	private MsgInboxVo selectLastReceivedMessage() {
		MsgInboxVo vo = (MsgInboxVo)msgInboxDao.getLastReceivedRecord();
		if (vo!=null) {
			System.out.println("MsgInboxDao - selectLastRecord: "+LF+vo);
		}
		return vo;
	}
}
