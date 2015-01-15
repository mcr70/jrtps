package net.sf.jrtps.udds.security;

import java.security.Key;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * Interface used to encode / decode an array of bytes.
 * 
 * @author mcr70
 */
public interface Transformer {
	/**
	 * Gets the id of this transformer. Id of the transformer is 
	 * transferred to remote entity, so that remote entity can 
	 * associate a correct CryptoTranformer to decode incoming message
	 *  
	 * @return id
	 */
	int getTransformationKind();
	
	/**
	 * Gets the name of this transformer. Name of the transformer 
	 * may be used in configuration file.
	 *  
	 * @return Name of the transformer
	 */
	String getName();
	
	/**
	 * Encodes RTPSByteBuffer into SecurePayload.
	 * @param bb RTPSByteBuffer 
	 * @return SecurePayload
	 * @throws SecurityException 
	 */
	SecurePayload encode(Key key, RTPSByteBuffer bb) throws SecurityException;
	
	/**
	 * Decodes a given SecurePayload
	 * @param payload Payload to decode
	 * @return RTPSByteBuffer
	 * @throws SecurityException
	 */
	RTPSByteBuffer decode(Key key, SecurePayload payload) throws SecurityException;
}
