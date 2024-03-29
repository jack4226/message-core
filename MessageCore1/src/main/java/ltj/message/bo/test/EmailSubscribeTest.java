package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import ltj.data.preload.RuleNameEnum;
import ltj.message.bean.MessageBean;
import ltj.message.bean.SimpleEmailSender;
import ltj.message.constant.Constants;
import ltj.message.constant.MsgDirection;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.dao.emailaddr.EmailSubscrptDao;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.message.vo.emailaddr.EmailSubscrptVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.spring.util.SpringAppConfig;
import ltj.spring.util.SpringJmsConfig;
import ltj.spring.util.SpringTaskConfig;

@ContextConfiguration(classes={SpringAppConfig.class, SpringJmsConfig.class, SpringTaskConfig.class})
@FixMethodOrder
public class EmailSubscribeTest extends BoTestBase {
	static final Logger logger = LogManager.getLogger(EmailSubscribeTest.class);
	@Resource
	private SimpleEmailSender mSend;
	@Resource
	private EmailSubscrptDao emailSubscrptDao;
	@Resource
	private MailingListDao mailingListDao;
	
	private static String testFromAddress;
	private static String mailingListAddr = Constants.DEMOLIST1_ADDR;

	@Test
	public void test1() {
		
		String digits = StringUtils.leftPad("" + new Random().nextInt(100), 2, "0");
		testFromAddress = "user" + digits + "@localhost";
		
		try {
			sendNotify("subscribe", "Test Subscription Body Message", mailingListAddr);
			//sendNotify("unsubscribe", "Test Subscription Body Message", mailingListAddr);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void test2() { // wait for mail reader to pick up the email
		try {
			Thread.sleep(90 * 1000L);
		} catch (InterruptedException e) {
		}
	}
	
	@Test
	public void test3() { // verifyDataRecord
		EmailAddressVo addrVo = selectEmailAddrByAddress(testFromAddress);
		assertNotNull(addrVo);
		List<MailingListVo> list = mailingListDao.getByAddress(mailingListAddr);
		assertTrue(list.size()>0);
		EmailSubscrptVo vo = emailSubscrptDao.getByAddrAndListId(addrVo.getEmailAddr(), list.get(0).getListId());
		assertNotNull(vo);
		assertEquals(Constants.Y, vo.getSubscribed());
		
		List<MsgInboxVo> miFrom = msgInboxDao.getByFromAddrId(addrVo.getEmailAddrId());
		assertFalse(miFrom.isEmpty());
		boolean foundFrom = false;
		for (MsgInboxVo mivo : miFrom) {
			if ("subscribe".equals(mivo.getMsgSubject())) {
				if (StringUtils.contains(mivo.getMsgBody(),"Test Subscription Body Message")) {
					if (mailingListAddr.equalsIgnoreCase(mivo.getToAddress())) {
						foundFrom = true;
						assertEquals(RuleNameEnum.SUBSCRIBE.name(), mivo.getRuleName());
						assertEquals(MsgDirection.RECEIVED.value(), mivo.getMsgDirection());
					}
				}
			}
		}
		assertEquals(true, foundFrom);
		
		List<MsgInboxVo> miTo = msgInboxDao.getByToAddrId(addrVo.getEmailAddrId());
		assertFalse(miTo.isEmpty());
		boolean foundTo = false;
		for (MsgInboxVo mivo : miTo) {
			if (StringUtils.contains(mivo.getMsgSubject(), "You have subscribed to mailing list")) {
				if (StringUtils.contains(mivo.getMsgBody(), "This is an automatically generated message to confirm")) {
					if (mailingListAddr.equals(mivo.getFromAddress())) {
						foundTo = true;
						assertEquals(RuleNameEnum.SEND_MAIL.name(), mivo.getRuleName());
						assertEquals(MsgDirection.SENT.value(), mivo.getMsgDirection());
					}
					
				}
			}
		}
		assertEquals(true, foundTo);
	}
	
	void sendNotify(String subject, String body, String user) {
		try {
			MessageBean mBean = new MessageBean();
			try {
				mBean.setFrom(InternetAddress.parse(testFromAddress, false));
				mBean.setTo(InternetAddress.parse(user, false));
			}
			catch (AddressException e) {
				logger.error("AddressException caught", e);
			}
			mBean.setSubject(subject);
			mBean.setValue(body + " " + new Date());
			mSend.sendMessage(mBean);
		}
		catch (Exception e) {
			logger.error("Exception caught during sendNotify()", e);
		}
	}

}
