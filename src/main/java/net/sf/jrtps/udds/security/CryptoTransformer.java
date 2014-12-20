package net.sf.jrtps.udds.security;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * Interface used to encode / decode an array of bytes.
 * 
 * @author mcr70
 */
public interface CryptoTransformer {
	/**
	 * Encodes RTPSByteBuffer into SecurePayload.
	 * @param bb RTPSByteBuffer 
	 * @return SecurePayload
	 */
	SecurePayload encode(RTPSByteBuffer bb);
	
	/**
	 * Decodes a given SecurePayload
	 * @param payload Payload to decode
	 * @return RTPSByteBuffer
	 */
	RTPSByteBuffer decode(SecurePayload payload);
}
