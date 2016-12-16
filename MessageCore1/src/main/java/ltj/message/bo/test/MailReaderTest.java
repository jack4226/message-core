package ltj.message.bo.test;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;

import ltj.message.bean.MessageBean;
import ltj.message.bean.SimpleEmailSender;
import ltj.message.bo.mailreader.MailReaderTaskExr;
import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.inbox.MsgInboxWebVo;
import ltj.message.vo.inbox.SearchFieldsVo;

@FixMethodOrder
public class MailReaderTest extends BoTestBase {
	static final Logger logger = Logger.getLogger(MailReaderTest.class);
	@Resource
	private SimpleEmailSender mSend;
	@Resource
	private MsgInboxDao inboxDao;
	@Resource
	private EmailAddrDao addrDao;
	
	private static String testFromAddr = "testfrom@localhost";
	
	private static Map<String, Integer> msgCountMap = new LinkedHashMap<>();;
 	
	@Test
	public void test1() { // MailReader
		try {
			int loops = 25; //Integer.MAX_VALUE;
			for (int i = 0; i < loops; i++) {
				String suffix = StringUtils.leftPad((i % 100) + "", 2, "0");
				String user = "user" + suffix + "@localhost";
				if (msgCountMap.containsKey(user)) {
					msgCountMap.put(user, msgCountMap.get(user) + 1);
				}
				else {
					msgCountMap.put(user, 1);
				}
//				if (i % 13 == 0) {
//					try {
//						Thread.sleep(1 * 1000);
//					}
//					catch (InterruptedException e) {
//						break;
//					}
//				}
				sendNotify("Test MailReader - " + suffix, "Test MailReader Body Message - " + suffix, user);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void test2() {
		MailReaderTaskExr.readTestUserAccounts = true;
		try {
			Thread.sleep(120 * 1000L);
		} catch (InterruptedException e) {
		}
	}
	
	@Test
	public void test3() { //verify results
		for (Iterator<String> it=msgCountMap.keySet().iterator(); it.hasNext();) {
			String toAddr = it.next();
			Integer count = msgCountMap.get(toAddr);
			EmailAddrVo toAddrVo = addrDao.getByAddress(toAddr);
			assertNotNull(toAddrVo);
			SearchFieldsVo vo = new SearchFieldsVo();
			vo.setFromAddr(testFromAddr);
			vo.setToAddrId(toAddrVo.getEmailAddrId());
			vo.setSubject("Test MailReader");
			List<MsgInboxWebVo> list = inboxDao.getListForWeb(vo);
			logger.info("Message count for (" + toAddr + ") expected = " + count + ", actual = " + list.size());
			assertTrue(list.size() >= count);
		}
	}
	
	void sendNotify(String subject, String body, String user) {
		try {
			MessageBean mBean = new MessageBean();
			try {
				mBean.setFrom(InternetAddress.parse(testFromAddr, false));
				mBean.setTo(InternetAddress.parse(user, false));
			}
			catch (AddressException e) {
				logger.error("AddressException caught", e);
			}
			mBean.setSubject(subject + " " + new Date());
			mBean.setValue(body);
			mSend.sendMessage(mBean);
		}
		catch (Exception e) {
			logger.error("Exception caught during sendNotify()", e);
		}
	}
	
	void sendVerpNotify(String subject, String body) {
		try {
			MessageBean mBean = new MessageBean();
			try {
				mBean.setFrom(InternetAddress.parse(testFromAddr, false));
				mBean.setTo(InternetAddress.parse("testto-10.07410251.0-jsmith=test.com@localhost"));
				//mBean.setCc(InternetAddress.parse("jwang@localhost,twang@localhost", false));
			}
			catch (AddressException e) {
				logger.error("AddressException caught", e);
			}
			mBean.setSubject(subject + " " + new Date());
			mBean.setValue(body);
			mSend.sendMessage(mBean);
		}
		catch (Exception e) {
			logger.error("Exception caught during sendNotify()", e);
		}
	}
}
