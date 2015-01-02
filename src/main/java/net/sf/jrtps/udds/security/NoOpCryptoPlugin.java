package net.sf.jrtps.udds.security;

import net.sf.jrtps.transport.RTPSByteBuffer;


/**
 * Registered with transformationId 0 to CryptoPlugin, which is treated
 * specially.
 *  
 * @author mcr70
 */
class NoOpTransformer implements CryptoTransformer {
	@Override
	public int getTransformationKind() {
		return 0;
	}

	@Override
	public String getName() {
		return "none";
	}

	@Override
	public SecurePayload encode(RTPSByteBuffer bb) {
		return null;
	}

	@Override
	public RTPSByteBuffer decode(SecurePayload payload) {
		return null;
	}
}
