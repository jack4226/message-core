package ltj.message.bo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.data.preload.RuleNameEnum;
import ltj.message.bean.MessageBean;
import ltj.message.bo.inbox.MessageParser;
import ltj.message.bo.task.TaskDispatcher;
import ltj.message.constant.Constants;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.dao.emailaddr.EmailSubscrptDao;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.message.vo.emailaddr.EmailSubscrptVo;
import ltj.message.vo.inbox.MsgInboxWebVo;

@FixMethodOrder
public class TaskDispatcherTest extends BoTestBase {
	@Resource
	private TaskDispatcher dispr;
	@Resource
	private MessageParser parser;
	@Resource
	private EmailSubscrptDao emailSubscrptDao;
	@Resource
	private MailingListDao mailingListDao;

	private static String testFromAddress;
	private static String mailingListAddr = Constants.DEMOLIST1_ADDR;
	private static MessageBean messageBean;
	
	@Test
	@Rollback(value=false)
	public void test0() throws Exception {
		String digits = StringUtils.leftPad("" + new Random().nextInt(100), 2, "0");
		testFromAddress = "user" + digits + "@localhost";
		emailAddressDao.findByAddress(testFromAddress);
	}
	
	@Test
	@Rollback(value=false)
	public void test1() throws Exception { // testTaskScheduler
		assertNotNull(dispr);
		messageBean = new MessageBean();
		
		messageBean.setFrom(InternetAddress.parse(testFromAddress));
		messageBean.setTo(InternetAddress.parse(mailingListAddr));
		messageBean.setSubject("subscribe");
		messageBean.setBody("Test Subscription Body Message");
		if (isDebugEnabled) {
			logger.debug("MessageBean created:" + LF + messageBean);
		}
		String ruleName = parser.parse(messageBean);
		messageBean.setRuleName(ruleName);
		
		dispr.dispatchTasks(messageBean);
	}
	
	@Test
	@Rollback(value=false)
	public void test2() { // wait for 5 seconds
		try {
			Thread.sleep(WaitTimeInMillis * 2);
		} catch (InterruptedException e) {
			//
		}
	}
	
	@Test
	public void test3() { // verifyDataRecord
		EmailAddressVo addrVo = selectEmailAddrByAddress(testFromAddress);
		assertNotNull("test from address must have been added to database.", addrVo);
		List<MailingListVo> list = mailingListDao.getByAddress(mailingListAddr);
		assertTrue(list.size() > 0);
		EmailSubscrptVo vo = emailSubscrptDao.getByAddrAndListId(addrVo.getEmailAddr(), list.get(0).getListId());
		assertNotNull("Subscription must have been added to database.", vo);
		assertEquals(Constants.Y, vo.getSubscribed());
		
		List<MsgInboxWebVo> msglist = selectMsgInboxByMsgRefId(messageBean.getMsgId());
		assertTrue(msglist.size() > 0);
		String subj = "You have subscribed to mailing list"; // Sample List 1";
		boolean found = false;
		for (MsgInboxWebVo mivo : msglist) {
			if (testFromAddress.equals(mivo.getToAddress())) {
				if (StringUtils.startsWith(mivo.getMsgSubject(), subj)) {
					found = true;
					assertEquals(RuleNameEnum.SEND_MAIL.name(), mivo.getRuleName());
				}
			}
		}
		assertEquals(true, found);
	}

}
