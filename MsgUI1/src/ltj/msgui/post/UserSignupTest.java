package ltj.msgui.post;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import ltj.message.dao.customer.CustomerDao;
import ltj.message.dao.emailaddr.EmailAddressDao;
import ltj.message.vo.emailaddr.EmailAddressVo;
import ltj.spring.util.SpringUtil;

public class UserSignupTest {
	static Logger logger = LogManager.getLogger(UserSignupTest.class);

	private final String USER_AGENT = "Mozilla/5.0";

	@Test
	public void testUserSignup() { // POST
		String url = "http://localhost:8080/MsgUI1/publicsite/usersignup.jsp";

		String prefix = StringUtils.leftPad(new Random().nextInt(1000) + "", 3, '0');
		
		EmailAddressVo emailVo = getEmailAddrDao().getRandomRecord();
		String emailAddr = prefix + "." + emailVo.getEmailAddr();
		
		try {
			java.net.URL obj = new java.net.URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

			StringBuilder sb = new StringBuilder();
			
			sb.append("testsignup=true&submit=Submit&emailAddr=" + emailAddr);
			
			sb.append("&firstName=Joe");
			sb.append("&lastName=Smith");
			sb.append("&userPswd=password");
			sb.append("&streetAddress=" + URLEncoder.encode("100 Main Street", "UTF-8"));
			sb.append("&cityName=Raleigh");
			sb.append("&stateCode=NC");
			sb.append("&zipCode5=27610");
			sb.append("&countryCode=US");
			sb.append("&securityQuestion=" + URLEncoder.encode("What is the name of your favorite pet?", "UTF-8"));
			sb.append("&securityAnswer=James");
			sb.append("&frompage=SignupJunitTest");

			String urlParameters = sb.toString();
			
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
			
			assertTrue(StringUtils.contains(rsp, "User Profile Details"));
			assertTrue(StringUtils.contains(rsp, "value=\"" + emailAddr + "\""));
			assertTrue(StringUtils.contains(rsp, "\"What is the name of your favorite pet?\" selected"));
			
			// clean up
			int rowsDeleted = getCustomerDao().deleteByEmailAddr(emailAddr);
			assertEquals(1, rowsDeleted);
			
			rowsDeleted = getEmailAddrDao().deleteByAddress(emailAddr);
			assertEquals(1, rowsDeleted);
		} catch (IOException e) {
			logger.error("IOException caught", e);
		}
	}
	
	private EmailAddressDao getEmailAddrDao() {
		return SpringUtil.getDaoAppContext().getBean(EmailAddressDao.class);
	}
	
	private CustomerDao getCustomerDao() {
		return SpringUtil.getDaoAppContext().getBean(CustomerDao.class);
	}
}
