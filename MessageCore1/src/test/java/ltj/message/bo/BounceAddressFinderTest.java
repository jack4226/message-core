package ltj.message.bo;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import ltj.message.bo.inbox.BounceAddressFinder;
import ltj.message.bo.test.BoTestBase;
import ltj.message.util.StringUtil;

public class BounceAddressFinderTest extends BoTestBase {
	static final Logger logger = LogManager.getLogger(BounceAddressFinderTest.class);
	static final boolean isDebugEnabled = logger.isDebugEnabled();
	
	static final String LF = System.getProperty("line.separator", "\n");

	@Test
	public void findBounceAddr() {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream is = loader.getResourceAsStream("bouncedmails/bounceBodySamples2.txt");
		if (is == null) {
			logger.warn("InputStream not found.");
			return;
		}
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		try {
			StringBuilder sb = new StringBuilder();
			String line = null;
			int count = 0;
			int matched = 0;
			while ((line = br.readLine()) != null) {
				if ("--- EOM ---".equals(line)) {
					count++;
					String body = sb.toString();
					sb = new StringBuilder();
					String addr = BounceAddressFinder.getInstance().find(body);
					if (StringUtil.isEmpty(addr)) {
						System.out.println("(" + count + ") - not matched ##########.");
					}
					else {
						System.out.println("(" + count + ") - matched: " + addr);
						matched++;
					}
				}
				else {
					sb.append(line + LF);
				}
			}
			br.close();
			logger.info("Count: " + count + ", Matched: " + matched);
			assertEquals(count, matched);
		}
		catch (IOException e) {
			logger.error("Exception", e);
			fail();
		}
	}

}
