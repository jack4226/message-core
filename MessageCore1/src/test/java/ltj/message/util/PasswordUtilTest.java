package ltj.message.util;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import ltj.jbatch.common.PasswordUtil;

public class PasswordUtilTest {
	static final Logger logger = LogManager.getLogger(PasswordUtilTest.class);
	
	@Test
	public void testAuthenticate() {
		String test_pswd = "test@pswd";
		try {
			byte[] salt = PasswordUtil.generateSalt();
			assertNotNull(salt);
			assertEquals(8, salt.length);

			byte[] pswd_byte = PasswordUtil.getEncryptedPassword(test_pswd, salt);
			assertNotNull(pswd_byte);
			assertEquals(20, pswd_byte.length);
			
			String pswd_hex = PasswordUtil.toHex(pswd_byte);
			logger.info("Encrypted Password in Hex: " + pswd_hex);
			
			assertEquals(40, pswd_hex.length());

			assertFalse(PasswordUtil.authenticate("test pswd", pswd_byte, salt));
			assertTrue(PasswordUtil.authenticate(test_pswd, pswd_byte, salt));
			
			assertTrue(PasswordUtil.authenticate(test_pswd, PasswordUtil.toHex(pswd_byte), PasswordUtil.toHex(salt)));
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
