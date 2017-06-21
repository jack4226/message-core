package ltj.message.main;

import static org.junit.Assert.*;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.log4j.Logger;
import org.junit.Test;

public class JcePbeTest {
	static final Logger logger = Logger.getLogger(JcePbeTest.class);

	@Test
	public void testEncryption() {
		try {
			PBEKeySpec pbeKeySpec;
		    PBEParameterSpec pbeParamSpec;
		    SecretKeyFactory keyFac;

		    // Salt
		    byte[] salt = {
		        (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c,
		        (byte)0x7e, (byte)0xc8, (byte)0xee, (byte)0x99
		    };

		    // Iteration count
		    int count = 20;

		    // Create PBE parameter set
		    pbeParamSpec = new PBEParameterSpec(salt, count);

			// Prompt user for encryption password.
			// Collect user password as char array (using the
			// "readPasswd" method from above), and convert
			// it into a SecretKey object, using a PBE key
			// factory. Use hard coded password for testing.
		    pbeKeySpec = new PBEKeySpec("passw0rd".toCharArray());
		    keyFac = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
		    SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);

		    // Create PBE Cipher
		    Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");

		    // Initialize PBE Cipher with key and parameters
		    pbeCipher.init(Cipher.ENCRYPT_MODE, pbeKey, pbeParamSpec);

		    String data = "This is another example";
		    // Our clear text
		    byte[] cleartext = data.getBytes();

		    // Encrypt the clear text
		    byte[] ciphertext = pbeCipher.doFinal(cleartext);
		    logger.info("Encrypted text length: " + ciphertext.length + " " + new String(ciphertext));
		    
		    pbeCipher.init(Cipher.DECRYPT_MODE, pbeKey, pbeParamSpec);
		    byte[] cleartext2 = pbeCipher.doFinal(ciphertext);
		    logger.info("Decrypted text: " + new String(cleartext2));
		    
		    assertEquals(data, new String(cleartext2));
		}
		catch (Exception e) {
			logger.error("Exception caught", e);
			fail();
		}
	}
}