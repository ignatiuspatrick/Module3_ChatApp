package Tests;

import static org.junit.jupiter.api.Assertions.*;

import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Security.SessionProtocol;

public class SessionProtocolTest {

	private SessionProtocol sess;
	private String s;
	private byte[] en;
	private byte[] de;
	private SecretKey sessKey; // session key

	@BeforeEach
	public void setUp() throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		sess = new SessionProtocol("hellogroup5");
		s = "hello world";
		sessKey = sess.generateSessionKey();
		en = sess.encryptPlainText(s.getBytes(), sessKey);
		de = sess.decryptPlainText(en, sessKey);
	}

	@Test
	public void testSecretKey() {
		assertTrue(sess.getSecretkey().getEncoded().length == 16);
		assertTrue(sess.getSecretkey() != null);
		assertTrue(sess.getSecretkey().getAlgorithm().equals("AES"));
	}

	@Test
	public void testGenerateSessionKey() {
		assertTrue(sess.generateSessionKey() != null);
	}

	@Test
	public void testEncryptPlainText() {
		assertFalse(en.equals(null));
	}

	@Test
	public void testDecryptPlainText() {
		assertEquals("hello world", new String(de));
	}

}
