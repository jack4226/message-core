package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.List;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.bean.MessageBean;
import ltj.message.bean.MessageBeanUtil;
import ltj.message.bo.inbox.MessageParser;
import ltj.message.bo.inbox.MsgInboxBo;
import ltj.message.bo.task.TaskDispatcher;
import ltj.message.util.EmailAddrUtil;
import ltj.message.util.FileUtil;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.inbox.MsgInboxVo;

@FixMethodOrder
public class RuleEngineTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(RuleEngineTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	@Resource
	MsgInboxBo msgInboxBo;
	@Resource
	private MessageParser messageParser;
	@Resource
	private TaskDispatcher taskDispatcher;
	
	final static int startFrom = 1;
	final static int loops = 1;
	
	private static String[] fromAddr = new String[loops];
	private static String[] toAddr = new String[loops];
	private static String[] ruleName = new String[loops];
	private static MessageBean[] messageBean = new MessageBean[loops];
	
	@Test
	@Rollback(value=false)
	public void test0() throws Exception {
		// insert email addresses here to work around the deadlock issue
		for (int i = 0; i < loops; i++) {
			byte[] mailStream = getBouncedMail(i + startFrom);
			messageBean[i] = MessageBeanUtil.createBeanFromStream(mailStream);
			messageBean[i].setIsReceived(true);
			fromAddr[i] = messageBean[i].getFromAsString();
			toAddr[i] = messageBean[i].getToAsString();
			assertNotNull(fromAddr[i]);
			assertNotNull(toAddr[i]);
			emailAddressDao.findByAddress(fromAddr[i]);
			emailAddressDao.findByAddress(toAddr[i]);
		}
	}
	
	@Test
	public void test1() throws Exception { // process bounced email
		for (int i = 0; i < loops; i++) {
			ruleName[i] = messageParser.parse(messageBean[i]);
			assertNotNull(ruleName[i]);
			taskDispatcher.dispatchTasks(messageBean[i]);
		}
	}
	
	@Test
	public void test2() { // wait for 5 seconds
		try {
			Thread.sleep(WaitTimeInMillis);
		} catch (InterruptedException e) {
			//
		}
	}

	@Test
	public void test3() { // verifyDataRecord
		for (int i = 0; i < loops; i++) {
			assertNotNull(messageBean[i].getMsgId());
			MsgInboxVo vo = selectMsgInboxByMsgId(messageBean[i].getMsgId());
			assertNotNull(vo);
			assertEquals(EmailAddrUtil.removeDisplayName(fromAddr[i]), vo.getFromAddress());
			assertEquals(EmailAddrUtil.removeDisplayName(toAddr[i]),vo.getToAddress());
			
			EmailAddressVo addrVo = selectEmailAddrByAddress(fromAddr[i]);
			assertNotNull(addrVo);
			
			List<MsgInboxVo> miList = msgInboxDao.getByFromAddrId(addrVo.getEmailAddrId());
			assertFalse(miList.isEmpty());
			assertEquals(ruleName[i], miList.get(miList.size() - 1).getRuleName());
			
			addrVo = selectEmailAddrByAddress(toAddr[i]);
			assertNotNull(addrVo);
			
			miList = msgInboxDao.getByToAddrId(addrVo.getEmailAddrId());
			assertFalse(miList.isEmpty());
			assertEquals(ruleName[i], miList.get(miList.size() - 1).getRuleName());
			
			assertEquals(messageBean[i].getSubject(), vo.getMsgSubject());
		}
	}

	byte[] getBouncedMail(int fileNbr) {
		return FileUtil.loadFromFile("bouncedmails", "BouncedMail_" + fileNbr + ".txt");
	}
}
