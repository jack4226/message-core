package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bean.SimpleEmailSender;
import ltj.message.constant.Constants;
import ltj.message.dao.emailaddr.MailingListDao;
import ltj.message.dao.emailaddr.SubscriptionDao;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.emailaddr.MailingListVo;
import ltj.message.vo.emailaddr.SubscriptionVo;

@FixMethodOrder
public class EmailSubscribeTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(EmailSubscribeTest.class);
	@Resource
	private SimpleEmailSender mSend;
	@Resource
	private SubscriptionDao subscriptionDao;
	@Resource
	private MailingListDao mailingListDao;
	
	private static String testFromAddress; // = "testfrom@localhost";
	private static String mailingListAddr = "demolist1@localhost";

	@Test
	public void test1() {
		
		String digits = StringUtils.leftPad("" + new Random().nextInt(1000), 4, "0");
		testFromAddress = "sbsr" + digits + "@localhost";
		
		try {
			sendNotify("subscribe", "Test Subscription Body Message", mailingListAddr);
			//sendNotify("unsubscribe", "Test Subscription Body Message", mailingListAddr);
			// TODO verify results
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void test2() {
		try {
			Thread.sleep(60000L);
		} catch (InterruptedException e) {
		}
	}
	
	@Test
	public void test3() { // verifyDataRecord
		EmailAddrVo addrVo = selectEmailAddrByAddress(testFromAddress);
		assertNotNull(addrVo);
		List<MailingListVo> list = mailingListDao.getByAddress(mailingListAddr);
		assertTrue(list.size()>0);
		SubscriptionVo vo = subscriptionDao.getByAddrAndListId(addrVo.getEmailAddr(), list.get(0).getListId());
		assertNotNull(vo);
		assertEquals(Constants.YES_CODE, vo.getSubscribed());
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
