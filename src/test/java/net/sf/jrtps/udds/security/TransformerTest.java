package net.sf.jrtps.udds.security;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.Assert;
import net.sf.jrtps.transport.RTPSByteBuffer;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransformerTest {
    private static final Logger logger = LoggerFactory.getLogger(CryptoPlugin.CRYPTO_LOG_CATEGORY);

	private byte[] bytes = new byte[] { 0x01, 0x02, 0x03, 0x04 };

	@Test
	public void testMacTransformer() throws NoSuchAlgorithmException, InvalidKeyException {
		MACTransformer tr = new MACTransformer("HmacSHA1", 0x02);
		SecretKeySpec key = createKey();
		RTPSByteBuffer bb = new RTPSByteBuffer(bytes);
		bb.getBuffer().position(bytes.length);
		
		SecurePayload payload = tr.encode(key, bb);
		logger.debug("mac encoded {} -> {}", Arrays.toString(bytes), 
				Arrays.toString(payload.getCipherText()));
		
		RTPSByteBuffer decoded = tr.decode(key, payload);
		logger.debug("mac decoded {} <- {}", Arrays.toString(decoded.getBuffer().array()), 
				Arrays.toString(payload.getCipherText()));
		
		Assert.assertTrue(Arrays.equals(bytes, decoded.getBuffer().array()));
	}
	
	@Test
	public void testCipherTransformer() throws NoSuchAlgorithmException, NoSuchPaddingException {
		CipherTransformer tr = new CipherTransformer("AES", 0x01);
		SecretKeySpec key = createKey();
		RTPSByteBuffer bb = new RTPSByteBuffer(bytes);
		bb.getBuffer().position(bytes.length);
		
		SecurePayload payload = tr.encode(key, bb);
		logger.debug("cipher encoded {} -> {}", Arrays.toString(bb.getBuffer().array()), 
				Arrays.toString(payload.getCipherText()));

		RTPSByteBuffer decoded = tr.decode(key, payload);
		logger.debug("cipher decoded {} <- {}", Arrays.toString(decoded.getBuffer().array()), 
				Arrays.toString(payload.getCipherText()));

		Assert.assertTrue(Arrays.equals(bytes, decoded.getBuffer().array()));
	}

	private SecretKeySpec createKey() {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] digest = md.digest("sharedsecret".getBytes());
			return new SecretKeySpec(digest, "AES");
		} catch (NoSuchAlgorithmException e) {
			Assert.fail();
		}

		return null;
	}	
}
