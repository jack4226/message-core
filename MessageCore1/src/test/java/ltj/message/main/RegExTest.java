package ltj.message.main;

import static org.junit.Assert.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.junit.Test;

public class RegExTest {
	static final Logger logger = Logger.getLogger(RegExTest.class);
	static final boolean isDebugEnabled = false; //logger.isDebugEnabled();
	
	static final String LF = System.getProperty("line.separator", "\n");
	
	@Test
	public void testRegex() {
		try {
			RegExTest test = new RegExTest();
			//test.general();
			assertTrue(test.isHTML("plain no html text <nonhtml abc> tag<a abc>"));
			assertTrue(test.isHTML("plain no html text <a> tag</a>"));
			String converted = test.convertUrlBraces("Web Beacon<img src='wsmopen.php?msgid=$%7BBroadcastMsgId%7D&amp;listid=$%7BListId%7D' width='1' height='1' alt=''>");
			assertEquals(converted, "Web Beacon<img src='wsmopen.php?msgid=${BroadcastMsgId}&amp;listid=${ListId}' width='1' height='1' alt=''>");
			java.util.Date date1 = test.extractDate("Thu, 09 Oct 2008 07:58:55 -0400 (EDT)");
			assertNotNull(date1);
			assertEquals("Thu Oct 09 07:58:55 EDT 2008", date1.toString());
			java.util.Date date2 = test.extractDate("9 Oct 2008 15:20:20 -0400");
			assertNotNull(date2);
			assertEquals("Thu Oct 09 15:20:20 EDT 2008", date2.toString());
			Pattern p = Pattern.compile("^user_name=([\\w\\s]{1,50});");
			Matcher m = p.matcher("user_name=test user;\\nuser_phone=;\\nkey please.");
			if (m.find() && m.groupCount() >= 1) {
				for (int i = 0; i <= m.groupCount(); i++) {
					logger.info("group [" + i + "] - " + m.group(i));
				}
				assertEquals(1, m.groupCount());
				assertEquals("test user", m.group(1));
			}
			String header = test.parseMessageHeader("Reporting-MTA: dns;MELMX.synnex.com.au");
			assertNotNull(header);
			assertEquals("Reporting-MTA:dns;MELMX.synnex.com.au", header);
			
			assertEquals(
					"K&quot;&amp;nbsp;L Gates LLP,<br/>K&amp;L Gates &amp; Center &#245; &amp;\n &amp;copy; &amp; &quot; &amp;",
					convertAmpersands("K&quot;&nbsp;L Gates LLP,<br/>K&L Gates & Center &#245; &\n &copy; &amp; &quot; &"));
		}
		catch (Exception e) {
			fail();
		}
	}

	String parseMessageHeader(String line) {
		Pattern ptn = Pattern.compile("^([\\w-]{1,50}): (.{1,100})$");
		Matcher m = ptn.matcher(line);
		if (m.find() && m.groupCount()>1) {
			return (m.group(1) + ":" + m.group(2));
		}
		return null;
	}
	
	java.util.Date extractDate(String text) {
		if (text == null) return null;
		String regex = "^(?:\\w{3},\\s+)??(\\d{1,2}\\s+\\w{3}\\s+\\d{4}\\s+\\d{2}:\\d{2}:\\d{2}\\s+[+-]\\d{4})";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(text);
		if (m.find() && m.groupCount() >= 1) {
			for (int i = 0; i <= m.groupCount(); i++) {
				logger.info("group [" + i + "] - " + m.group(i));
			}
			String tsStr = m.group(1);
			SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy HH:mm:ss Z");
			try {
				java.util.Date date = sdf.parse(tsStr);
				return date;
			}
			catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	/*
	 * convert %7B %7D pair to {}
	 */
	String convertUrlBraces(String text) {
		String regex = "\\$\\%7B(.{1,26}?)\\%7D";
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher m = p.matcher(text);
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			for (int i = 0; i <= m.groupCount(); i++) {
				logger.info("group [" + i + "]: " + m.group(i));
			}
			m.appendReplacement(sb, "\\$\\{" + m.group(1)+"\\}");
		}
		m.appendTail(sb);
		return (sb.toString());
	}
	
	String convertAmpersands(String htmlData) {
		String regex = "&((?!(?:quot|amp|apos|lt|gt|#\\d{3,6}|#x\\w{3,5});))";
		return htmlData.replaceAll(regex, "&amp;$1");
	}

	boolean isHTML(String text) {
		Pattern pattern0 = Pattern.compile("<([a-zA-Z]{1,10})(?:\\b.{1,1024}?)?>(.*?)</\\1>", Pattern.DOTALL);
		Matcher m0 = pattern0.matcher(text);
		if (m0.find()) {
			if (isDebugEnabled) {
				logger.debug("Pattern0: " + m0.group(m0.groupCount()));
				logger.debug("Pattern0: " + m0.group(0));
				logger.debug("Pattern0: " + m0.group(1));
			}
		}
		Pattern pattern1 = Pattern.compile("<([a-z]{1,10})[\\s]?[\\/]?>", Pattern.CASE_INSENSITIVE);
		Matcher m1 = pattern1.matcher(text);
		boolean f1 = false, f2 = false;
		if ((f1 = m1.find())) {
			if (isDebugEnabled) {
				logger.debug("Pattern1: " + m1.group(m1.groupCount()));
				logger.debug("Pattern1: " + m1.group(0));
			}
		}
		Pattern pattern2 = Pattern.compile("<([a-z]{1,10})(?:\\b.{0,1024}?[\\/]?)?>",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher m2 = pattern2.matcher(text);
		if ((f2 = m2.find())) {
			if (isDebugEnabled) {
				logger.debug("Pattern2: " + m2.group(m2.groupCount()));
				logger.debug("Pattern2: " + m2.group(0));
			}
		}
		return f1 | f2;
	}
	
	@Test
	public void testGeneral() {
		Pattern pattern = Pattern.compile("^[\\[<\"](.+)[\\]>\"]$");
		String addr = "[subscribe]";
		Matcher m = pattern.matcher(addr);
		if (m.find(0)) {
			addr = m.group(m.groupCount());
			logger.info("Pattern Found ? " + m.find(0) + ", " + addr);
			assertEquals(true, m.find(0));
		}
		if (isDebugEnabled) {
			logger.debug("abc\r\ndef".replaceAll("\n", " ").replaceAll("\r", " "));
		}
		
		String ssnRegex = "^([0-6]\\d{2}|7[0-6]\\d|77[0-2])([ \\-]?)(\\d{2})\\2(\\d{4})$";
		ssnRegex = "^(?!000)(?:[0-6]\\d{2}|7(?:[0-6]\\d|7[0-2]))([ -]?)(?!00)\\d{2}\\1(?!0000)\\d{4}$";
		pattern = Pattern.compile(ssnRegex);
		m = pattern.matcher("078-05-1120");
		if (isDebugEnabled) {
			logger.debug("Valid SSN (078-05-1120) ? " + m.matches());
		}
		assertEquals(true, m.matches());
		
		String phoneRegex = "^(?:1[ -]?)?(?:\\(\\d{3}\\)|\\d{3})[ -]?(?:\\d{3}|[a-z]{3})[ -]?(?:\\d{4}|[a-z]{4})$";
		pattern = Pattern.compile(phoneRegex, Pattern.CASE_INSENSITIVE);
		m = pattern.matcher("1(614)123-CELL");
		if (isDebugEnabled) {
			logger.debug("Valid Phone# (1(614)123-CELL) ? " + m.matches());
		}
		assertEquals(true, m.matches());
		
		pattern = Pattern.compile("(?i)^<?[a-z0-9-~#&\\_]+(?:\\.[a-z0-9-~#&\\_]+)*@(?:[a-z0-9-]+\\.)+[a-z]{2,5}>?\\s*$");
		pattern = Pattern.compile("^<?.+@.+>?$");
		m = pattern.matcher("<>");
		logger.info("Pattern matched (<>) ? " + m.matches());
		assertEquals(false, m.matches());
	}
}
