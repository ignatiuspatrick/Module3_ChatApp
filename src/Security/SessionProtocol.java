package Security;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class SessionProtocol {

	private static final String ALGORITHM = "AES";
	private Cipher cipher;
	private SecretKeySpec authKey;

	public SessionProtocol(String password) {
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			authKey = generateSecretKey(password);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}

	public SecretKeySpec getAuthKey() {
		return this.authKey;
	}

	// generate secret key from password
	public SecretKeySpec generateSecretKey(String password) {
		SecretKeySpec specKey = null;
		try {
			byte[] pass = password.getBytes("UTF-8"); // get bytes of the pass
			MessageDigest sha = MessageDigest.getInstance("SHA-1"); // generate hash function
			pass = sha.digest(pass);
			pass = Arrays.copyOf(pass, 16); // take 16 bits of the hash function
			specKey = new SecretKeySpec(pass, ALGORITHM);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return specKey;
	}

	// generate random secret key
	public SecretKey generateSessionKey() {
		// generate key
		KeyGenerator keyGen;
		SecretKey sessionKey = null;
		try {
			keyGen = KeyGenerator.getInstance(ALGORITHM);
			SecureRandom random = new SecureRandom();
			keyGen.init(random);
			sessionKey = keyGen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return sessionKey;
	}

	// encrypt plain text with AES
	public byte[] encryptPlainText(byte[] plainText, SecretKey s) {
		byte[] encText = null;

		try {
			// initiate cipher
			cipher.init(Cipher.ENCRYPT_MODE, s);

			// encrypt plain text
			encText = cipher.doFinal(plainText);

		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}

		return encText;
	}

	public byte[] decryptPlainText(byte[] encText, SecretKey s) {
		byte[] decText = null;
		try {
			// initiate cipher
			cipher.init(Cipher.DECRYPT_MODE, s);

			// decrypt text
			decText = cipher.doFinal(encText);

		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return decText;
	}

	public byte[] generateMAC(byte[] encText) {
		return null;
	}

	public boolean compareMAC(byte[] mac1, byte[] mac2) {
		return false;
	}

	public static String asHex(byte buf[]) {
		StringBuffer strbuf = new StringBuffer(buf.length * 2);
		int i;

		for (i = 0; i < buf.length; i++) {
			if (((int) buf[i] & 0xff) < 0x10)
				strbuf.append("0");

			strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
		}
		return strbuf.toString();
	}

	public static void main(String[] args) {
		SessionProtocol s = new SessionProtocol();
		byte[] e = s.encryptPlainText("Hello World".getBytes());
		byte[] d = s.decryptPlainText(e, s.getSecretKey());
		String encryptedString = new String(e);
		String originalString = new String(d);
		System.out.println(encryptedString);
		System.out.println(originalString);
	}

}
