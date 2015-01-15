package net.sf.jrtps.udds.security;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Mac;

import net.sf.jrtps.transport.RTPSByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MacTransformer calculates and appends a HMAC to the end of payload.
 * 
 * @see javax.crypto.Mac
 * @author mcr70
 */
class MACTransformer implements Transformer {
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

	/**
	 * Constructor for MACTransformer. An Instance of Mac is obtained by calling
	 * Mac.getInstance(hmacName).
	 * 
	 * @param hmacName name of the MAC algorithm.
	 * @param kind transformationKind
	 * @throws NoSuchAlgorithmException
	 */
	MACTransformer(String hmacName, int kind) throws NoSuchAlgorithmException {
		this.hmacName = hmacName;
		this.kind = kind;
		this.hmac = Mac.getInstance(hmacName);
		this.hmacLength = hmac.getMacLength();
	}
	
	@Override
	public SecurePayload encode(Key key, RTPSByteBuffer bb) throws SecurityException {
		ByteBuffer buffer = bb.getBuffer();
		buffer.flip();
		byte[] hmacBytes;
		synchronized (hmac) {
			try {
				hmac.init(key);
			} catch (InvalidKeyException e) {
				throw new SecurityException(e);
			}
			
			hmac.update(buffer);
			hmacBytes = hmac.doFinal();					
		}

		byte[] array = buffer.array();
		byte[] securedBytes = new byte[buffer.limit() + hmacBytes.length];
		
		System.arraycopy(array, 0, securedBytes, 0, buffer.limit());
		System.arraycopy(hmacBytes, 0, securedBytes, buffer.limit(), hmacBytes.length);
		
		SecurePayload sp = new SecurePayload(kind, securedBytes);
		
		return sp;
	}

	@Override
	public RTPSByteBuffer decode(Key key, SecurePayload payload) throws SecurityException {
		byte[] cipherText = payload.getCipherText();
		if (logger.isTraceEnabled()) {
			logger.trace("MACTransformer: decoding {}", Arrays.toString(cipherText));
		}

		byte[] hmacReceived = new byte[hmacLength];
		byte[] payloadBytes = new byte[cipherText.length - hmacReceived.length];
		
		System.arraycopy(cipherText, 0, payloadBytes, 0, payloadBytes.length);
		System.arraycopy(cipherText, payloadBytes.length, hmacReceived, 0, hmacReceived.length);

		byte[] hmacCalculated = null;
		synchronized (hmac) {
			try {
				hmac.init(key);
			} catch (InvalidKeyException e) {
				// TODO: better exception handling
				throw new RuntimeException(e);
			}

			hmacCalculated = hmac.doFinal(payloadBytes);
		}

		if (!Arrays.equals(hmacReceived, hmacCalculated)) {
			logger.debug("Hmacs different: {} != {}", hmacReceived, hmacCalculated);
			throw new SecurityException("Hmac of SecurePayload different"); // TODO: DecodeException
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
}
