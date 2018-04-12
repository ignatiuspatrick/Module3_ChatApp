package Security;

import java.awt.RenderingHints.Key;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class PublicKeyAlgorithm {

	private static final String ALGORITHM = "RSA";
	private static int compID;
	private Key publicKey;
	private Key privateKey;

	public PublicKeyAlgorithm(int compID) {
		this.compID = compID;
		setUpKeys();
	}

	public void setUpKeys() {
		KeyPairGenerator kpg;
		try {
			kpg = KeyPairGenerator.getInstance(ALGORITHM);
			kpg.initialize(2048);
			KeyPair kp = kpg.genKeyPair();
			PublicKey publicKey = kp.getPublic();
			PrivateKey privateKey = kp.getPrivate();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public byte[] dencryptPublicKey(PublicKey publicKey, byte[] inputData) {
		byte[] encryptedBytes = null;
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.PUBLIC_KEY, publicKey);
			encryptedBytes = cipher.doFinal(inputData);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return encryptedBytes;
	}

	public byte[] dencryptPrivateKey(PrivateKey privateKey, byte[] inputData) {
		Cipher cipher;
		byte[] encryptedBytes = null;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.PRIVATE_KEY, privateKey);
			encryptedBytes = cipher.doFinal(inputData);
			return encryptedBytes;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return encryptedBytes;
	}

	public Key getPublicKey() {
		return this.publicKey;
	}

	public Key getPrivateKey() {
		return this.privateKey;
	}

	public static void main(String[] args) {
		PublicKeyAlgorithm p = new PublicKeyAlgorithm(1);
		byte[] e = p.dencryptPrivateKey((PrivateKey) p.getPrivateKey(), "Hello World!".getBytes());
		System.out.println(new String(e));
		byte[] d = p.dencryptPublicKey((PublicKey) p.getPublicKey(), e);
		System.out.println(new String(d));
	}
}
