package ltj.message.bo.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.springframework.test.annotation.Rollback;

import ltj.message.bean.MessageBean;
import ltj.message.bo.TaskDispatcher;
import ltj.message.bo.inbox.MessageParser;
import ltj.message.constant.Constants;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.dao.emailaddr.SubscriptionDao;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.message.vo.emailaddr.SubscriptionVo;

public class TaskDispatcherTest extends BoTestBase {
	@Resource
	private TaskDispatcher dispr;
	@Resource
	private MessageParser parser;
	@Resource
	private SubscriptionDao subscriptionDao;
	@Resource
	private MailingListDao mailingListDao;

	private String testFromAddress; // = "testfrom@localhost";
	private String mailingListAddr = "demolist1@localhost";
	
	@Test
	@Rollback(value=false)
	public void testTaskScheduler() throws Exception {
		assertNotNull(dispr);
		MessageBean messageBean = buildMessageBeanFromMsgStream();
		
		String digits = StringUtils.leftPad("" + new Random().nextInt(1000), 4, "0");
		testFromAddress = "sbsr" + digits + "@localhost";
		
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
		// TODO verify results
		Thread.sleep(5000L);
		verifyDataRecord();
	}
	
	private void verifyDataRecord() {
		EmailAddrVo addrVo = selectEmailAddrByAddress(testFromAddress);
		assertNotNull(addrVo);
		List<MailingListVo> list = mailingListDao.getByAddress(mailingListAddr);
		assertTrue(list.size()>0);
		SubscriptionVo vo = subscriptionDao.getByAddrAndListId(addrVo.getEmailAddr(), list.get(0).getListId());
		assertNotNull(vo);
		assertEquals(Constants.YES_CODE, vo.getSubscribed());
	}


}
