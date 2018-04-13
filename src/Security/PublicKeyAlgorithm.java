package Security;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class PublicKeyAlgorithm {

	private static final String ALGORITHM = "RSA";

	public PublicKeyAlgorithm(int compID) {
	}

	public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {

		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);

		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");

		// 512 is keysize
		keyGen.initialize(512, random);

		KeyPair generateKeyPair = keyGen.generateKeyPair();
		return generateKeyPair;
	}

	public static byte[] encryptPublicKey(byte[] publicKey, byte[] inputData) {
		byte[] encryptedBytes = null;
		Cipher cipher;
		try {
			PublicKey pubKey = KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(publicKey));
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.PUBLIC_KEY, pubKey);
			encryptedBytes = cipher.doFinal(inputData);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		}
		return encryptedBytes;
	}

	public static byte[] decryptPrivateKey(byte[] privateKey, byte[] inputData) {
		Cipher cipher;
		byte[] decryptedBytes = null;
		PrivateKey privKey;
		try {
			privKey = KeyFactory.getInstance(ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(privateKey));
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.PRIVATE_KEY, privKey);
			decryptedBytes = cipher.doFinal(inputData);
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
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

		return decryptedBytes;

	}

	public static byte[] encryptPrivateKey(byte[] privateKey, byte[] inputData) {
		byte[] encryptedBytes = null;

		PrivateKey privKey;
		try {
			privKey = KeyFactory.getInstance(ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(privateKey));
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, privKey);
			encryptedBytes = cipher.doFinal(inputData);
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
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

	public static byte[] decryptPublicKey(byte[] publicKey, byte[] inputData) {
		byte[] decryptedBytes = null;
		PublicKey pubKey;
		try {
			pubKey = KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(publicKey));
			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, pubKey);
			decryptedBytes = cipher.doFinal(inputData);
		} catch (InvalidKeySpecException e) {
			e.printStackTrace();
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

		return decryptedBytes;
	}

	public static void main(String[] args) {
		KeyPair k;
		try {
			String test = "Bhenchod";
			k = generateKeyPair();
			byte[] e1 = encryptPublicKey(k.getPublic().getEncoded(), test.getBytes());
			System.out.println(new String(e1));
			byte[] d1 = decryptPrivateKey(k.getPrivate().getEncoded(), e1);
			System.out.println(new String(d1));
			System.out.println("------------------------------");
			byte[] e2 = encryptPrivateKey(k.getPrivate().getEncoded(), test.getBytes());
			System.out.println(new String(e2));
			byte[] d2 = decryptPublicKey(k.getPublic().getEncoded(), e2);
			System.out.println(new String(d2));
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		} catch (NoSuchProviderException e1) {
			e1.printStackTrace();
		}

	}
}
