package net.sf.jrtps.udds.security;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

import net.sf.jrtps.qos.AbstractQosTest;

import org.junit.Test;

public class CryptoTest extends AbstractQosTest {
	@Test
	public void testHmac() throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException, InvalidKeyException {
        for (Provider p : Security.getProviders()) {
        	System.out.println("- " + p.getName());
        }
		KeyStore ks = KeyStore.getInstance("JKS");

        InputStream is = getClass().getResourceAsStream("/jrtps.jks");
        ks.load(is, "changeit".toCharArray());
        Key key = ks.getKey("jrtps01", "jrtps01".toCharArray());
        System.out.println(key.getAlgorithm());
        
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
		SecretKey generateKey = keyGenerator.generateKey();
		
		byte[] bytes = new byte[] {0x00, 0x01, 0x02, 0x03, 0x04, 0x04};
		System.out.println(bytes.length + ":" + Arrays.toString(bytes));

		Mac hmac = Mac.getInstance("HmacSHA1");
		hmac.init(generateKey);
		byte[] hmac_bytes = hmac.doFinal(bytes);
		long l1 = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			hmac_bytes = hmac.doFinal(bytes);
		}
		long l2 = System.currentTimeMillis();
		System.out.println(hmac_bytes.length + ":" + Arrays.toString(hmac_bytes));
		System.out.println("- " + (l2-l1));
		
		hmac.reset();

		hmac = Mac.getInstance("HmacMD5");
		hmac.init(generateKey);
		l1 = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			hmac_bytes = hmac.doFinal(bytes);
		}
		l2 = System.currentTimeMillis();
		System.out.println(hmac_bytes.length + ":" + Arrays.toString(hmac_bytes));
		System.out.println("- " + (l2-l1));
		
		hmac.reset();

		hmac = Mac.getInstance("HmacSHA256");
		hmac.init(generateKey);
		l1 = System.currentTimeMillis();
		for (int i = 0; i < 100; i++) {
			hmac_bytes = hmac.doFinal(bytes);
		}
		l2 = System.currentTimeMillis();
		System.out.println(hmac_bytes.length + ":" + Arrays.toString(hmac_bytes));
		System.out.println("- " + (l2-l1));
		hmac.reset();
	}
}
