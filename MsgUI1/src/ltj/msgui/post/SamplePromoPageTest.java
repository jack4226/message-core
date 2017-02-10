package ltj.msgui.post;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.dao.emailaddr.SubscriptionDao;
import ltj.message.dao.inbox.MsgClickCountsDao;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.emailaddr.SubscriptionVo;
import ltj.message.vo.inbox.MsgClickCountsVo;
import ltj.spring.util.SpringUtil;

public class SamplePromoPageTest {
	static Logger logger = Logger.getLogger(SamplePromoPageTest.class);

	private final String USER_AGENT = "Mozilla/5.0";

	@Test
	public void testSbsrIdClickCount() { // POST
		String url = "http://localhost:8080/MsgUI1/publicsite/SamplePromoPage.jsp";
		
		SubscriptionVo subsVo = getSubscriptionDao().getRandomRecord();
		assertNotNull(subsVo);
		long sbsrId = subsVo.getEmailAddrId();
		String listId = subsVo.getListId();

		EmailAddrVo emailVo = getEmailAddrDao().getByAddrId(sbsrId);
		assertNotNull(emailVo);

		try {
			java.net.URL obj = new java.net.URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			String urlParameters = "sbsrid=" + sbsrId + "&listid=" + listId;

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
			
			assertTrue(StringUtils.contains(rsp, "listid=" + listId + "&sbsrid=" + sbsrId));
			assertTrue(StringUtils.contains(rsp, "msgid=${BroadcastMsgId}"));
		} catch (IOException e) {
			logger.error("IOException caught", e);
		}
	}
	
	@Test
	public void testMsgIdClickCount() { // GET
		MsgClickCountsVo countVo = getMsgClickCountsDao().getRandomRecord();
		assertNotNull(countVo);
		long msgId = countVo.getMsgId();
		
		String url = "http://localhost:8080/MsgUI1/publicsite/SamplePromoPage.jsp?msgid=" + msgId;
		
		try {
			java.net.URL obj = new java.net.URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			// add request header
			con.setRequestProperty("User-Agent", USER_AGENT);

			int responseCode = con.getResponseCode();
			logger.info("Sending 'GET' request to URL : " + url);
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
			
			assertTrue(StringUtils.contains(rsp, "msgid=" + msgId));
			assertTrue(StringUtils.contains(rsp, "listid=${MailingListId}&sbsrid=${SubscriberAddressId}"));
		} catch (IOException e) {
			logger.error("IOException caught", e);
		}
	}
	
	private EmailAddrDao getEmailAddrDao() {
		return SpringUtil.getDaoAppContext().getBean(EmailAddrDao.class);
	}
	
	private SubscriptionDao getSubscriptionDao() {
		return SpringUtil.getDaoAppContext().getBean(SubscriptionDao.class);
	}
	
	private MsgClickCountsDao getMsgClickCountsDao() {
		return SpringUtil.getDaoAppContext().getBean(MsgClickCountsDao.class);
	}
	
}