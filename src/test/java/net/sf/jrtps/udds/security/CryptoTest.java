package net.sf.jrtps.udds.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

public class CryptoTest {
	byte[] bytes = new byte[] {0x00, 0x01, 0x02, 0x03, 0x04, 0x04};
	final int COUNT = 1000;
	KeyStore ks;
	
	@Before
	public void init() throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		ks = KeyStore.getInstance("JKS");
		InputStream is = getClass().getResourceAsStream("/jrtps.jks");
		ks.load(is, "changeit".toCharArray());
	}
	
	@Test
	public void testCertificate() throws KeyStoreException, InvalidKeyException, CertificateException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException { 
		Certificate cert1 = ks.getCertificate("jrtps01");
		Certificate cert2 = ks.getCertificate("jrtps02");
		Certificate cert3 = ks.getCertificate("jrtps03");
		Certificate ca = ks.getCertificate("jrtpsCA");
		
		cert1.verify(ca.getPublicKey());
		cert2.verify(ca.getPublicKey());
		
		try {
			cert3.verify(ca.getPublicKey());
			Assert.fail("cert03 was signed by CA");
		} catch (InvalidKeyException | CertificateException
				| NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) {
			
		}
	}
	
	@Test
	public void testHmac() throws InvalidKeyException, NoSuchAlgorithmException {

		KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
		keyGenerator.init(128);

		SecretKey sKey = keyGenerator.generateKey();

		testHmac(sKey, Mac.getInstance("HmacSHA1"));
		testHmac(sKey, Mac.getInstance("HmacMD5"));
		testHmac(sKey, Mac.getInstance("HmacSHA256"));
	}

	private void testHmac(SecretKey sKey, Mac hmac) throws InvalidKeyException {
		byte[] hmac_bytes = null;
		hmac.init(sKey);
		long l1 = System.currentTimeMillis();
		for (int i = 0; i < COUNT; i++) {
			hmac_bytes = hmac.doFinal(bytes);
		}
		long l2 = System.currentTimeMillis();
		System.out.println(hmac.getAlgorithm() + " in " + (l2-l1) +" ms, "+ ", " + hmac_bytes.length + ":" + 
				Arrays.toString(hmac_bytes));

		hmac.reset();		
	}

	@Test
	public void testSigning() throws Exception {
		byte[] bytes = new byte[] {0x01, 0x02, 0x03, 0x04};

        String alias = "jrtps01";
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
        Key privateKey = ks.getKey(alias, alias.toCharArray());
        
		Signature signature = Signature.getInstance("SHA1withRSA");
		signature.initSign((PrivateKey)privateKey);
		
		signature.update(bytes);
		byte[] signatureBytes = signature.sign();
		
		signature.initVerify(cert.getPublicKey());
		signature.verify(signatureBytes);
	}
	
	
	@Test
	public void testRSACipher() throws Exception {
		System.out.println("testRSACipher()");

		String alias = "jrtps01";
		
		X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, cert.getPublicKey());
		
		byte[] doFinal = cipher.doFinal(bytes);
		
		System.out.println(Arrays.toString(bytes) + " -> " + Arrays.toString(doFinal));
		
		cipher.init(Cipher.DECRYPT_MODE, ks.getKey(alias, alias.toCharArray()));
		byte[] decrypted = cipher.doFinal(doFinal);
		
		System.out.println(Arrays.toString(decrypted) + " <- " + Arrays.toString(doFinal));
	}

	@Test
	public void testAESCipher() throws Exception {
		System.out.println("testAESCipher()");
		byte[] sharedSecret = new byte[] {0x11, 0x12, 0x13, 0x14};
		
		String hashName = "MD5"; // MD5(128), SHA-1(160), SHA-256(256)
		byte[] key = hash(hashName , sharedSecret); 
		System.out.println("* " + key.length + ", " + key.length * 8);
		Cipher cipher = Cipher.getInstance("AES");
		SecretKeySpec k = new SecretKeySpec(key, "AES");
		cipher.init(Cipher.ENCRYPT_MODE, k);
		
		byte[] doFinal = cipher.doFinal(bytes);
		
		System.out.println(Arrays.toString(bytes) + " -> " + Arrays.toString(doFinal));
		
		cipher.init(Cipher.DECRYPT_MODE, k);
		byte[] decrypted = cipher.doFinal(doFinal);
		
		System.out.println(Arrays.toString(decrypted) + " <- " + Arrays.toString(doFinal));
	}

	public byte[] hash(String hashName, byte[] convertme) throws NoSuchAlgorithmException {
	    MessageDigest md = MessageDigest.getInstance(hashName);
	        
	    return md.digest(convertme);
	}	
}
