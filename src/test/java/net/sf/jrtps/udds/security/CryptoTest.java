package net.sf.jrtps.udds.security;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

import net.sf.jrtps.qos.AbstractQosTest;

import org.junit.Test;

public class CryptoTest {
	byte[] bytes = new byte[] {0x00, 0x01, 0x02, 0x03, 0x04, 0x04};
	final int COUNT = 1000;
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
	public void testRSACipher() throws Exception {
		//Security.getProvider("").get
		for (Provider provider: Security.getProviders()) {
			System.out.println(provider.getName());
			for (String key: provider.stringPropertyNames())
				System.out.println("\t" + key + "\t" + provider.getProperty(key));
		}

		KeyStore ks = KeyStore.getInstance("JKS");
		InputStream is = getClass().getResourceAsStream("/jrtps.jks");
		ks.load(is, "changeit".toCharArray());

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
}
