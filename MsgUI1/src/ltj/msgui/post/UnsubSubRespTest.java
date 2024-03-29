package ltj.msgui.post;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.FixMethodOrder;
import org.junit.Test;

import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.emailaddr.EmailSubscrptDao;
import ltj.message.dao.inbox.MsgClickCountDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.util.PrintUtil;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.emailaddr.EmailSubscrptVo;
import ltj.message.vo.inbox.MsgClickCountVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.spring.util.SpringUtil;

@FixMethodOrder
public class UnsubSubRespTest {
	static Logger logger = LogManager.getLogger(UnsubSubRespTest.class);

	private final String USER_AGENT = "Mozilla/5.0";

	private static long sbsrId = -1L;
	private static String listId = null;
	private static long msgId = -1L;
	
	private UnsubTestBase testBase = new UnsubTestBase();
	
	/*
	 * Put three tests in one class to make it repeatable.
	 */
	
	@Test
	public void test1() { // 1) Find a subscriber and UnSubscribe
		String url = "http://localhost:8080/MsgUI1/publicsite/MsgUnsubRespPage.jsp";
		
		EmailSubscrptVo subsVo = getSubscriptionDao().getRandomRecord();
		assertNotNull(subsVo);
		sbsrId = subsVo.getEmailAddrId();
		listId = subsVo.getListId();

		EmailAddressVo emailVo = getEmailAddrDao().getByAddrId(sbsrId);
		assertNotNull(emailVo);
		
		MsgClickCountVo countVo = getMsgClickCountsDao().getRandomRecord();
		assertNotNull(countVo);
		msgId = countVo.getMsgId();
		
		try {
			java.net.URL obj = new java.net.URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			String urlParameters = "submit=true&sbsrid=" + sbsrId + "&listid=" + listId + "&msgid=" + msgId;

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			logger.info("Sending 'POST' request to URL : " + url);
			logger.info("Post parameters : " + urlParameters);
			logger.info("Response Code : " + responseCode);

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			String rsp = response.toString();
			logger.info(rsp);
			
			if (StringUtils.contains(rsp, "Your email address has been removed from our following newsletters:")) {
				assertTrue(StringUtils.contains(rsp, listId));
				assertTrue(StringUtils.contains(rsp, emailVo.getEmailAddr()));
				assertTrue(StringUtils.contains(rsp, "sbsrid=" + emailVo.getEmailAddrId()));
				
				try { // wait for the out-bound email to persist in database
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
				}
				
				List<MsgInboxVo> list = getMsgInboxDao().getByToAddrId(emailVo.getEmailAddrId());
				assertFalse(list.isEmpty());
				
				MsgInboxVo msgVo = list.get(list.size() - 1);
				logger.info(PrintUtil.prettyPrint(msgVo));
				assertEquals(msgVo.getMsgSubject(), "You have unsubscribed from our Newsletter");
				assertTrue(StringUtils.contains(msgVo.getMsgBody(), emailVo.getEmailAddr()));
			}
			else if (StringUtils.contains(rsp, "You have already un-subscribed from the list")) {
				assertTrue(StringUtils.contains(rsp, "Un-subscription Confirmation"));
			}
			else {
				fail();
			}
		} catch (IOException e) {
			logger.error("IOException caught", e);
		}
	}
	
	@Test
	public void test2() { // 2) Subscribe again
		testBase.subscribe(sbsrId, listId);
	}
	
	@Test
	public void test3() { // 3) Confirm subscription
		testBase.confirm(sbsrId, listId);
	}
	
	private EmailAddressDao getEmailAddrDao() {
		return SpringUtil.getDaoAppContext().getBean(EmailAddressDao.class);
	}
	
	private EmailSubscrptDao getSubscriptionDao() {
		return SpringUtil.getDaoAppContext().getBean(EmailSubscrptDao.class);
	}
	
	private MsgClickCountDao getMsgClickCountsDao() {
		return SpringUtil.getDaoAppContext().getBean(MsgClickCountDao.class);
	}
	
	private MsgInboxDao getMsgInboxDao() {
		return SpringUtil.getDaoAppContext().getBean(MsgInboxDao.class);
	}
}
