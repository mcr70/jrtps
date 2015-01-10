package net.sf.jrtps.udds.security;

import java.security.Key;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * CompositeTransformer joins two transformations. During encoding,
 * bytes are first encoded with the first Tranformer. Result of the first transformation
 * is input to second transformation. Decoding is performed on reverse order. 
 * 
 * @author mcr70
 */
class CompositeTransformer implements Transformer {
	public static final String AES_HMAC_SHA1_NAME = "aes_hmac_sha1";
    public static final String AES_HMAC_SHA256_NAME = "aes_hmac_sha256";
    public static final int AES_HMAC_SHA1 = 0xff000200;
    public static final int AES_HMAC_SHA256 = 0xff000201;
	
	private final Transformer tr1;
	private final int kind;
	private final String name;
	private final Transformer tr2;
	
	public CompositeTransformer(int kind, Transformer tr1, Transformer tr2) {
		this.name = tr1.getName() + "+" + tr2.getName();
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
	 * Name of this CompositeTransformer. Name is composed of the names of 
	 * Transformers, so that name is tr1.name+tr2.name. For example, "AES+HmacSHA1"  
	 * @return name
	 */
	@Override
	public String getName() {
		return name;
	}

	@Override
	public SecurePayload encode(Key key, RTPSByteBuffer bb) {
		SecurePayload sp1 = tr1.encode(key, bb);
		SecurePayload sp2 = tr2.encode(key, new RTPSByteBuffer(sp1.getCipherText()));
		
		return sp2;
	}

	@Override
	public RTPSByteBuffer decode(Key key, SecurePayload payload) {
		RTPSByteBuffer bb1 = tr2.decode(key, payload);
		RTPSByteBuffer bb2 = tr1.decode(key, new SecurePayload(bb1)); 
		
		return bb2;
	}
}
