package net.sf.jrtps.udds.security;

import java.security.Key;

import net.sf.jrtps.transport.RTPSByteBuffer;


/**
 * Registered with transformationId 0 to CryptoPlugin, which is treated
 * specially.
 *  
 * @author mcr70
 */
class NoOpTransformer implements Transformer {
	@Override
	public int getTransformationKind() {
		return 0;
	}

	@Override
	public String getName() {
		return "none";
	}

	@Override
	public SecurePayload encode(Key key, RTPSByteBuffer bb) {
		return null;
	}

	@Override
	public RTPSByteBuffer decode(Key key, SecurePayload payload) {
		return null;
	}
}
