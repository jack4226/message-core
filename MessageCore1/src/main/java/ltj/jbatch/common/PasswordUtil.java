package ltj.jbatch.common;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.log4j.Logger;

public class PasswordUtil {
	static final Logger logger = Logger.getLogger(PasswordUtil.class);

	public static PasswordTuple getEncryptedPassword(String clearPassword) {
		PasswordTuple tuple = new PasswordTuple();
		try {
			byte[] salt = generateSalt();
			byte[] pswd = getEncryptedPassword(clearPassword, salt);
			tuple.password = toHex(pswd);
			tuple.salt = toHex(salt);
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException e) { // should never happen
			logger.error("Exception caught", e);
			throw new RuntimeException(e);
		}
		return tuple;
	}
	
	public static boolean authenticate(String attemptedPassword, String encryptedPassword, String salt) {
		// Encrypt the clear-text password using the same salt that was used to
		// encrypt the original password
		byte[] encryptedAttemptedPassword = null;
		try {
			encryptedAttemptedPassword = getEncryptedPassword(attemptedPassword, fromHex(salt));
		}
		catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			logger.error("Exception caught", e);
			throw new RuntimeException(e);
		}

		// Authentication succeeds if encrypted password that the user entered
		// is equal to the stored hash
		return areHashesEqual(encryptedPassword, encryptedAttemptedPassword);
	}
	
	public static boolean authenticate(String attemptedPassword, byte[] encryptedPassword, byte[] salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		// Encrypt the clear-text password using the same salt that was used to
		// encrypt the original password
		byte[] encryptedAttemptedPassword = getEncryptedPassword(attemptedPassword, salt);

		// Authentication succeeds if encrypted password that the user entered
		// is equal to the stored hash
		return areHashesEqual(encryptedPassword, encryptedAttemptedPassword);
	}

	public static byte[] getEncryptedPassword(String password, byte[] salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		// PBKDF2 with SHA-1 as the hashing algorithm. Note that the NIST
		// specifically names SHA-1 as an acceptable hashing algorithm for
		// PBKDF2
		String algorithm = "PBKDF2WithHmacSHA1";
		// SHA-1 generates 160 bit hashes, so that's what makes sense here
		int derivedKeyLength = 160;
		// Pick an iteration count that works for you. The NIST recommends at
		// least 1,000 iterations:
		// http://csrc.nist.gov/publications/nistpubs/800-132/nist-sp800-132.pdf
		// iOS 4.x reportedly uses 10,000:
		// http://blog.crackpassword.com/2010/09/smartphone-forensics-cracking-blackberry-backup-passwords/
		int iterations = 20000;

		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterations, derivedKeyLength);

		SecretKeyFactory f = SecretKeyFactory.getInstance(algorithm);

		return f.generateSecret(spec).getEncoded();
	}

	public static byte[] generateSalt() throws NoSuchAlgorithmException {
		// VERY important to use SecureRandom instead of just Random
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

		// Generate a 8 byte (64 bit) salt as recommended by RSA PKCS5
		byte[] salt = new byte[8];
		random.nextBytes(salt);

		return salt;
	}

	public static String toHex(byte[] array) {
		BigInteger bi = new BigInteger(1, array);
		String hex = bi.toString(16);
		int paddingLength = (array.length * 2) - hex.length();
		if (paddingLength > 0) {
			return String.format("%0" + paddingLength + "d", 0) + hex;
		} else {
			return hex;
		}
	}

	public static byte[] fromHex(String hex) {
		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
		}
		return bytes;
	}
	
	static boolean areHashesEqual(String hash1, byte[] hash2) {
		return Arrays.equals(fromHex(hash1), hash2);
	}

	static boolean areHashesEqual(byte[] hash1, byte[] hash2) {
		/*
		int diff = hash1.length ^ hash2.length;
		for (int i = 0; i < hash1.length && i < hash2.length; i++) {
			diff |= hash1[i] ^ hash2[i];
		}
		return diff == 0;
		*/
		return Arrays.equals(hash1, hash2);
	}

	public static class PasswordTuple {
		String password;
		String salt;
		
		public String getPassword() {
			return password;
		}
		public String getSalt() {
			return salt;
		}
	}
	
	public static void main(String[] args) {
		try {
			byte[] salt = generateSalt();

			byte[] password = getEncryptedPassword("test@pswd", salt);
			logger.info("Length in byte - salt: " + salt.length + ", pswd: " + password.length);
			logger.info("Encrypted Password: " + toHex(password));

			logger.info("Should be False -> " + authenticate("test pswd", password, salt));
			logger.info("Should be  True -> " + authenticate("test@pswd", password, salt));
			
			logger.info("Should be  True -> " + authenticate("test@pswd", toHex(password), toHex(salt)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
