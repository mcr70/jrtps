package net.sf.jrtps.udds.security;

import java.security.Key;

import net.sf.jrtps.transport.RTPSByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CompositeTransformer joins two transformations. During encoding,
 * bytes are first encoded with the first Tranformer. Result of the first transformation
 * is input to second transformation. Decoding is performed on reverse order. 
 * 
 * @author mcr70
 */
class CompositeTransformer implements Transformer {
	private static final Logger logger = LoggerFactory.getLogger(CryptoPlugin.CRYPTO_LOG_CATEGORY);
	
	public static final int AES_HMAC_SHA1 = 0xffff0200;
    public static final int AES_HMAC_SHA256 = 0xffff0201;
	
	private final Transformer tr1;
	private final int kind;
	private final String name;
	private final Transformer tr2;
	
	public CompositeTransformer(int kind, Transformer tr1, Transformer tr2) {
		this.name = tr1.getName() + tr2.getName();
		this.kind = kind;
		this.tr1 = tr1;
		this.tr2 = tr2;
		
		if (tr1 == null || tr2 == null) {
			throw new IllegalArgumentException("Transformer cannot be null");
		}
	}

	@Override
	public int getTransformationKind() {
		return kind;
	}

	/**
	 * Name of this CompositeTransformer. Name is composed by concatenating
	 * names of the Transformers. For example AESHmacSHA1.
	 *   
	 * @return name
	 */
	@Override
	public String getName() {
		return name;
	}

	@Override
	public SecurePayload encode(Key key, RTPSByteBuffer bb) {
		byte[] originalBytes = bb.toArray();

		// Encode with first Transformer
		SecurePayload sp1 = tr1.encode(key, bb);
		RTPSByteBuffer bb2 = new RTPSByteBuffer(sp1.getCipherText());
		bb2.getBuffer().position(sp1.getCipherText().length);		
		
		// Encode with second Transformer
		SecurePayload sp2 = tr2.encode(key, bb2);
		
		// Return SecurePayload with transformationKind of _this_ Transformer
		return new SecurePayload(kind, sp2.getCipherText());
	}

	@Override
	public RTPSByteBuffer decode(Key key, SecurePayload payload) {
		// Decode first with second Transformer
		RTPSByteBuffer bb1 = tr2.decode(key, payload);
		// Decode next with first Transformer
		RTPSByteBuffer bb2 = tr1.decode(key, new SecurePayload(tr1.getTransformationKind(), bb1.getBuffer().array()));
		
		return bb2;
	}
}
