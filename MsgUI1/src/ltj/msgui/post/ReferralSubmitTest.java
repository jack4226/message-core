package ltj.msgui.post;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

import ltj.message.dao.emailaddr.EmailAddrDao;
import ltj.message.dao.emailaddr.SubscriptionDao;
import ltj.message.dao.inbox.MsgClickCountsDao;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.util.PrintUtil;
import ltj.message.vo.emailaddr.EmailAddrVo;
import ltj.message.vo.emailaddr.SubscriptionVo;
import ltj.message.vo.inbox.MsgClickCountsVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.spring.util.SpringUtil;

public class ReferralSubmitTest {
	static Logger logger = Logger.getLogger(ReferralSubmitTest.class);

	private final String USER_AGENT = "Mozilla/5.0";

	@Test
	public void testSbsrIdClickCount() { // POST
		String url = "http://localhost:8080/MsgUI1/publicsite/referral.jsp";
		
		SubscriptionVo subsVo = getSubscriptionDao().getRandomRecord();
		assertNotNull(subsVo);
		long sbsrId = subsVo.getEmailAddrId();
		String listId = subsVo.getListId();

		EmailAddrVo emailVo = getEmailAddrDao().getByAddrId(sbsrId);
		assertNotNull(emailVo);
		
		EmailAddrVo referrer = getEmailAddrDao().getRandomRecord();
		assertNotNull(referrer);
		
		MsgClickCountsVo countVo = getMsgClickCountsDao().getRandomRecord();
		assertNotNull(countVo);
		long msgId = countVo.getMsgId();

		try {
			java.net.URL obj = new java.net.URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			String yourName = "ReferralTest";
			String comments = "This is a pretty cool web site, please give it shot!";
			
			String urlParameters = "submit=Send&sbsrid=" + sbsrId + "&listid=" + listId + "&msgid=" + msgId
					+ "&rcptEmail=" + emailVo.getEmailAddr() + "&emailtype=html&yourName=" + yourName + "&yourEmail="
					+ referrer.getEmailAddr() + "&frompage=referralUnitTest&comments" + comments;

			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			logger.info("Sending 'POST' request to URL : " + url);
			logger.info("Post parameters : " + URLEncoder.encode(urlParameters, "UTF-8"));
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
			
			assertTrue(StringUtils.contains(rsp, sbsrId + ""));
			assertTrue(StringUtils.contains(rsp, listId));
			assertTrue(StringUtils.contains(rsp, emailVo.getEmailAddr()));
			assertTrue(StringUtils.contains(rsp, referrer.getEmailAddr()));
			assertTrue(StringUtils.contains(rsp, msgId + ""));
			
			try { // wait for the out-bound email to persist in database
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
			}
			
			List<MsgInboxVo> list = getMsgInboxDao().getByToAddrId(emailVo.getEmailAddrId());
			assertFalse(list.isEmpty());
			
			MsgInboxVo msgVo = list.get(list.size() - 1);
			logger.info(PrintUtil.prettyPrint(msgVo));
			assertTrue(StringUtils.contains(msgVo.getMsgSubject(), yourName));
			assertTrue(StringUtils.contains(msgVo.getMsgBody(), emailVo.getEmailAddr()));
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
	
	private MsgInboxDao getMsgInboxDao() {
		return SpringUtil.getDaoAppContext().getBean(MsgInboxDao.class);
	}
}
