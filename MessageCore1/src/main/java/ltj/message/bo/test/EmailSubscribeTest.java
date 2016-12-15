package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bean.SimpleEmailSender;
import ltj.message.constant.Constants;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.dao.emailaddr.SubscriptionDao;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.message.vo.emailaddr.SubscriptionVo;

public class EmailSubscribeTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(EmailSubscribeTest.class);
	@Resource
	private SimpleEmailSender mSend;
	@Resource
	private SubscriptionDao subscriptionDao;
	@Resource
	private MailingListDao mailingListDao;
	
	private String testFromAddress = "testfrom@localhost";
	private String mailingListAddr = "demolist1@localhost";

	@Test
	public void testSendNotify() {
		try {
			sendNotify("subscribe", "Test Subscription Body Message", mailingListAddr);
			//sendNotify("unsubscribe", "Test Subscription Body Message", mailingListAddr);
			// TODO verify results
			Thread.sleep(60000L);
			verifyDataRecord();
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
				mBean.setFrom(InternetAddress.parse(testFromAddress, false));
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
