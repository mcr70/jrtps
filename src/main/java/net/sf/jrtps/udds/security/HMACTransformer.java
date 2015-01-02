package net.sf.jrtps.udds.security;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import net.sf.jrtps.transport.RTPSByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that calculates and appends a HMAC to the end of payload.
 * 
 * @author mcr70
 */
class HMACTransformer implements CryptoTransformer {
	private static final Logger logger = LoggerFactory.getLogger(CryptoPlugin.CRYPTO_LOG_CATEGORY);

	// See 9.5.2.2 DDS:Crypto:AES-CTR-HMAC-RSA/DSA-DH CryptoTransformIdentifier
    // for predefined transformation_kind_id values: 
    public static final int HMAC_SHA1 = 0x00000100;
    public static final int HMAC_SHA256 = 0x00000101;

	public static final String HMAC_SHA1_NAME = "HmacSHA1";
	public static final String HMAC_SHA256_NAME = "HmacSHA256";
	
	private final String hmacName;
	private final int kind;
	private final Mac hmac;
	private final int hmacLength;


	HMACTransformer(String hmacName, int kind) throws NoSuchAlgorithmException {
		this.hmacName = hmacName;
		this.kind = kind;
		this.hmac = Mac.getInstance(hmacName);
		this.hmacLength = hmac.getMacLength();
		
		try {
			hmac.init(createKey("MD5", "sharedsecret".getBytes()));
		} catch (InvalidKeyException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public SecurePayload encode(RTPSByteBuffer bb) {
		ByteBuffer buffer = bb.getBuffer();
		int position = buffer.position();

		hmac.reset();
		hmac.update(buffer);
		buffer.position(position);

		byte[] hmacBytes = hmac.doFinal();		
		buffer.put(hmacBytes);

		byte[] array = buffer.array();
		if (logger.isTraceEnabled()) {
			byte[] payload = new byte[position];
			System.arraycopy(array, 0, payload, 0, payload.length);
			
			logger.trace("HMACTransformer: encoded {}, {}", Arrays.toString(payload), Arrays.toString(hmacBytes));
		}
		
		// TODO: use ByteBuffer instead of array with SecurePayload to avoid unnecessary
		//       array copy
		
		byte[] securedBytes = new byte[buffer.position()];
		System.arraycopy(array, 0, securedBytes, 0, securedBytes.length);		
		SecurePayload sp = new SecurePayload(kind, securedBytes);
		
		return sp;
	}

	@Override
	public RTPSByteBuffer decode(SecurePayload payload) {
		byte[] cipherText = payload.getCipherText();
		if (logger.isTraceEnabled()) {
			logger.trace("HMACTransformer: decoding {}", Arrays.toString(cipherText));
		}

		byte[] hmacReceived = new byte[hmacLength];
		byte[] payloadBytes = new byte[cipherText.length - hmacReceived.length];
		
		System.arraycopy(cipherText, 0, payloadBytes, 0, payloadBytes.length);
		System.arraycopy(cipherText, payloadBytes.length, hmacReceived, 0, hmacReceived.length);

		synchronized (hmac) {
			hmac.reset();
			byte[] hmacCalculated = hmac.doFinal(payloadBytes);
			if (Arrays.equals(hmacReceived, hmacCalculated)) {
				throw new SecurityException("Hmac of SecurePayload different"); // TODO: DecodeException
			}
		}
		
		RTPSByteBuffer bb = new RTPSByteBuffer(payloadBytes);
		
		return bb;
	}

	@Override
	public int getTransformationKind() {
		return kind;
	}

	@Override
	public String getName() {
		return hmacName;
	}

	private SecretKeySpec createKey(String hashName, byte[] convertme) throws NoSuchAlgorithmException {
	    MessageDigest md = MessageDigest.getInstance(hashName);
	    byte[] digest = md.digest(convertme);

		return new SecretKeySpec(digest, "AES");
	}	
}
