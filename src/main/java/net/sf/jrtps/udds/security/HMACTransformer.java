package net.sf.jrtps.udds.security;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;

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
	
	private Mac hmac;
	private int kind;
	private String hmacName;

	HMACTransformer(String hmacName, int kind) throws NoSuchAlgorithmException {
		this.hmacName = hmacName;
		this.kind = kind;
		this.hmac = Mac.getInstance(hmacName);
	}
	
	@Override
	public SecurePayload encode(RTPSByteBuffer bb) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RTPSByteBuffer decode(SecurePayload payload) {
		// TODO Auto-generated method stub
		return null;
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
