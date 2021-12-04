package ltj.msgui.post;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import ltj.message.dao.idtokens.MsgIdCipher;

public class ConfirmSubsTest {
	static Logger logger = LogManager.getLogger(ConfirmSubsTest.class);

	private final String USER_AGENT = "Mozilla/5.0";

	@Test
	public void sendPost() {
		String url = "http://localhost:8080/MsgUI1/publicsite/confirmsub.jsp";
		try {
			java.net.URL obj = new java.net.URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			String sbsrId = MsgIdCipher.encode(1L);
			String urlParameters = "sbsrid=" + sbsrId + "&listids=SMPLLST1&sbsraddr=jsmith@test.com";

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
			assertTrue(StringUtils.contains(rsp, "You have already confirmed the subscriptions."));
		} catch (IOException e) {
			logger.error("IOException caught", e);
		}
	}
	
	@Test
	public void sendGet() {
		String sbsrId = MsgIdCipher.encode(1L);
		String url = "http://localhost:8080/MsgUI1/publicsite/confirmsub.jsp?sbsrid=" + sbsrId + "&listids=SMPLLST1&sbsraddr=confirmsub@localhost";
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
			
			assertTrue(StringUtils.contains(rsp, "Subscription Confirmation"));
			assertTrue(StringUtils.contains(rsp, "You have already confirmed the subscriptions."));
			
		} catch (IOException e) {
			logger.error("IOException caught", e);
		}
	}
}
