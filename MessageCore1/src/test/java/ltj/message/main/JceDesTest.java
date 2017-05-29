package ltj.message.main;

import static org.junit.Assert.*;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.junit.Before;
import org.junit.Test;

public class JceDesTest {

	private SecretKey desKey;
	private Cipher desCipher;
	
	@Before
	public void setup() {
		try {
		KeyGenerator keygen = KeyGenerator.getInstance("DES");
	    desKey = keygen.generateKey();
	    
	    // Create the cipher 
	    desCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			e.printStackTrace();
			fail();
		}
	}
	
	@Test
	public void testEncryption1() {
		testEncryptDecrypt("This is just an example");
		testEncryptDecrypt("00000123");
	}
	
	private void testEncryptDecrypt(String data) {
		try {
		    // Initialize the cipher for encryption
		    desCipher.init(Cipher.ENCRYPT_MODE, desKey);
		    
		    // Our clear text
		    byte[] cleartext = data.getBytes();
		
		    // Encrypt the clear text
		    byte[] ciphertext = desCipher.doFinal(cleartext);
		    for (int i=0; i<ciphertext.length; i++) {
		    	//byte b = ciphertext[i];
		    	//System.out.println("Encrypted byte[" + i + "]: " + b);
		    }
		    System.out.println("Encrypted text: " + new String(ciphertext));
		
		    // Initialize the same cipher for decryption
		    desCipher.init(Cipher.DECRYPT_MODE, desKey);
		
		    // Decrypt the cipher text
		    byte[] cleartext1 = desCipher.doFinal(ciphertext);
		    
		    System.out.println("Decrypted text: " + new String(cleartext1));
		    assertEquals(data, new String(cleartext1));
		}
		catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}
}