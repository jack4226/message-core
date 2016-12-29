package ltj.message.util;

import static org.junit.Assert.*;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import ltj.jbatch.app.HostUtil;
import ltj.jbatch.common.ProductKey;
import ltj.message.bean.HtmlConverter;

public class MultiUtilsTest {
	static final Logger logger = Logger.getLogger(MultiUtilsTest.class);
	
	@Test
	public void testPhoneNumberUtil() {
		assertTrue(PhoneNumberUtil.isValidPhoneNumber("1(900)123-4567"));
		assertFalse(PhoneNumberUtil.isValidPhoneNumber("1(9xy)123-4567"));
		
		String converted1 = PhoneNumberUtil.convertPhoneLetters("1 614-JOe-Cell");
		logger.info("Converted Phone Number 1: " + converted1);
		assertEquals("1 614-563-2355", converted1);
		String converted2 = PhoneNumberUtil.convertTo10DigitNumber("1 614-JO6-G0LO");
		logger.info("Converted Phone Number 2: " + converted2);
		assertEquals("6145664056", converted2);
	}
	
	@Test
	public void testSsnNumber() {
		assertEquals(true, SsnNumberUtil.isValidSSN("078 05 1120"));
		assertEquals(false, SsnNumberUtil.isValidSSN("987-65-4320"));
	}
	
	@Test
	public void testProductKey() {
		assertTrue(ProductKey.validateKey("K51BJ-A0U7X-K97CX-0TYDQ-ED5AA"));
		assertFalse(ProductKey.validateKey("K51BJ-A0U7X-K97CX-0TYDQ-ED5AB"));
	}
	
	@Test
	public void testHtmlConverter() {
		try {
			// the HTML to convert
			byte[] bytes = FileUtil.loadFromFile("htmldocs/","index.html");
			HtmlConverter parser = HtmlConverter.getInstance();
			String text = parser.convertToText(new String(bytes));
			System.out.println(text);
			assertTrue(StringUtils.startsWith(text.trim(), "Java Agent Container"));
			assertTrue(StringUtils.contains(text, "low level JMS logic"));
			assertTrue(StringUtils.endsWith(StringUtil.trimRight(text), "Agent's runtime <status>status."));
		} catch (Exception e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testHtmlTags() {
		assertEquals(true, HtmlTags.isHTML("<p>Dear ${CustomerName}, <br></p><p>This is test template message body to ${SubscriberAddress}.</p><p>Time sent: ${CurrentDate}</p><p>Contact Email: ${ContactEmailAddress}</p><p>To unsubscribe from this mailing list, send an e-mail to: ${MailingListAddress}with \"unsubscribe\" (no quotation marks) in the subject of the message.</p>"));
		assertEquals(true, HtmlTags.isHTML("plain no html text <nonhtml abc> tag<a abc>def"));
		assertEquals(true, HtmlTags.isHTML("plain no html text <!DOCTYPE abcde> tag<aa abc>"));
		assertEquals(true, HtmlTags.isHTML("<H1 LAST_MODIFIED=\"1194988178\">Bookmarks</H1>"));
	}
	
	@Test
	public void testHostUtilBasic() {
		
		assertNotNull(HostUtil.getHostIpAddress());
		
		assertNotNull(HostUtil.getHostName());
		
	}
	
	@Test
	@Ignore
	public void testHostUtilAdvanced() {
		
		List<String> nmList = HostUtil.getNetworkInterfaceNames(true);
		if (nmList.isEmpty()) {
			logger.warn("Failed to find a \"Up\" interface!!!!! get all interfaces instead:");
			nmList = HostUtil.getNetworkInterfaceNames(false);
		}
		assertFalse(nmList.isEmpty());
		logger.info("Network Interface names: " + nmList);
		
		for (String name : nmList) {
			List<String> ipList = HostUtil.getByNetworkInterfaceName(name);
			assertFalse(ipList.isEmpty());
			logger.info(name + " IPs: " + ipList);
			List<String> ipv4List = HostUtil.getIPListByNetworkInterfaces();
			assertFalse(ipv4List.isEmpty());
			logger.info("IPv4 list: " + ipv4List);
			boolean found = false;
			for (String ip : ipList) {
				if (found == false) {
					found = ipv4List.contains(ip);
				}
			}
			assertEquals(true, found);
		}
	}

}
