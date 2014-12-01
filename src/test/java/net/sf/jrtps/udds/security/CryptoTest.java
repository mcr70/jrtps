package net.sf.jrtps.udds.security;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

import net.sf.jrtps.qos.AbstractQosTest;

import org.junit.Test;

public class CryptoTest extends AbstractQosTest {
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
	public void testCipher() {
		
	}
}
