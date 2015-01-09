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
	private final Transformer tr1;
	private final int kind;
	private final String name;
	private final Transformer tr2;

	public CompositeTransformer(String name, int kind, Transformer tr1, Transformer tr2) {
		this.name = name;
		this.kind = kind;
		this.tr1 = tr1;
		this.tr2 = tr2;
	}

	@Override
	public int getTransformationKind() {
		return kind;
	}

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
		RTPSByteBuffer decode = tr2.decode(key, payload);
		tr1.decode(key, null); // TODO: read SecurePayload from RTPSByteBuffer
		
		return null;
	}
}
