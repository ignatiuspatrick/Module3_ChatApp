package Security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class SessionProtocol {

	private static final String ALGORITHM = "AES";
	private Cipher cipher;
	private SecretKey sessionKey;

	public SessionProtocol() {
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			generateSecretKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}

	public void generateSecretKey() {
		// generate key
		KeyGenerator keyGen;
		try {
			keyGen = KeyGenerator.getInstance(ALGORITHM);
			SecureRandom random = new SecureRandom();
			keyGen.init(random);
			sessionKey = keyGen.generateKey();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}

	// encrypt plain text with AES
	public byte[] encryptPlainText(byte[] plainText) {
		byte[] encText = null;

		try {
			// initiate cipher
			cipher.init(Cipher.ENCRYPT_MODE, sessionKey);

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

	public SecretKey getSecretKey() {
		return this.sessionKey;
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
