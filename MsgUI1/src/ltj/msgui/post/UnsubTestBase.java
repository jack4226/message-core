package ltj.msgui.post;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.dao.idtokens.MsgIdCipher;
import ltj.message.dao.inbox.MsgInboxDao;
import ltj.message.util.PrintUtil;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.message.vo.inbox.MsgInboxVo;
import ltj.spring.util.SpringUtil;

public class UnsubTestBase {
	static Logger logger = Logger.getLogger(UnsubTestBase.class);
	
	private final String USER_AGENT = "Mozilla/5.0";
	
	void subscribe(final long sbsrId, final String listId) {
		String url = "http://localhost:8080/MsgUI1/publicsite/subscribeResp.jsp";

		EmailAddressVo emailVo = getEmailAddrDao().getByAddrId(sbsrId);
		assertNotNull(emailVo);
		
		try {
			java.net.URL obj = new java.net.URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			String urlParameters = "submit=true&sbsrAddr=" + emailVo.getEmailAddr() + "&emailtype=html&chosen=" + listId;

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
			
			assertTrue(StringUtils.contains(rsp, emailVo.getEmailAddr()));
			assertTrue(StringUtils.contains(rsp, listId));
			assertTrue(StringUtils.contains(rsp, "sbsrid=" + emailVo.getEmailAddrId()));
			
			try { // wait for the out-bound email to persist in database
				Thread.sleep(1000L);
			} catch (InterruptedException e) {
			}
			
			List<MsgInboxVo> list = getMsgInboxDao().getByToAddrId(emailVo.getEmailAddrId());
			assertFalse(list.isEmpty());
			
			MsgInboxVo msgVo = list.get(list.size() - 1);
			logger.info(PrintUtil.prettyPrint(msgVo));
			assertEquals(msgVo.getMsgSubject(), "Request for subscription confirmation");
			assertTrue(StringUtils.contains(msgVo.getMsgBody(), emailVo.getEmailAddr()));
		} catch (IOException e) {
			logger.error("IOException caught", e);
			fail();
		}
	}
	
	void confirm(final long sbsrId, final String listId) {
		String url = "http://localhost:8080/MsgUI1/publicsite/confirmsub.jsp";
		
		EmailAddressVo emailVo = getEmailAddrDao().getByAddrId(sbsrId);
		assertNotNull(emailVo);
		
		try {
			java.net.URL obj = new java.net.URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			String sbsrIdEnc = MsgIdCipher.encode(sbsrId);
			String urlParameters = "sbsrid=" + sbsrIdEnc + "&listids=" + listId + "&sbsraddr=" + emailVo.getEmailAddr();

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
			
			assertTrue(StringUtils.contains(rsp, "Subscription Confirmation"));
			assertTrue(StringUtils.contains(rsp, listId));
			assertTrue(StringUtils.contains(rsp, emailVo.getEmailAddr()));
		} catch (IOException e) {
			logger.error("IOException caught", e);
			fail();
		}
	}
	
	private EmailAddressDao getEmailAddrDao() {
		return SpringUtil.getDaoAppContext().getBean(EmailAddressDao.class);
	}

	private MsgInboxDao getMsgInboxDao() {
		return SpringUtil.getDaoAppContext().getBean(MsgInboxDao.class);
	}

}
