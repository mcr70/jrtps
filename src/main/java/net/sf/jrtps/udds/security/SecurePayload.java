package net.sf.jrtps.udds.security;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * SecurePayload.
 * 
 * @author mcr70
 */
public class SecurePayload {
	private final int transformationKind;  // long, one of predefined integers above
	private final byte[] transformationId; // octet[8]
	private final byte[] cipherText;       // octet[*]

	/**
	 * Reads SecurePayload from given RTPSByteBuffer
	 * @param bb
	 */
	SecurePayload(RTPSByteBuffer bb) {
		this.transformationKind = bb.read_long();
		this.transformationId = new byte[8];
		bb.read(transformationId);
		
		this.cipherText = new byte[bb.read_long()];
		bb.read(cipherText);
	}
	
	public SecurePayload(int transformationKind, byte[] cipherText) {
		this(transformationKind, new byte[8], cipherText);
	}
	
	public SecurePayload(int transformationKind, byte[] transformationId, byte[] cipherText) {
		this.transformationKind = transformationKind;
		this.transformationId = transformationId;
		this.cipherText = cipherText;
		
        if (transformationId == null || transformationId.length != 8) {
            throw new IllegalArgumentException("transformationId must be a byte array of length 8");
        }
        
        if (cipherText == null) {
            throw new IllegalArgumentException("cipherText cannot be null");
        }
	}
	
	/**
	 * Gets transformation kind
	 * @return transformationKind
	 */
	public int getTransformationKind() {
		return transformationKind;
	}
	
	/**
	 * Gets transformationId. Transformation id is a byte array of length 8.
	 * @return transformationId
	 */
	public byte[] getTrasformationId() {
		return transformationId;
	}
	
	/**
	 * Gets the cipher text
	 * @return cipherText
	 */
	public byte[] getCipherText() {
		return cipherText;
	}

	/**
	 * Writes this SecurePayload to given RTPSByteBuffer
	 * @param bb
	 */
	public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(transformationKind);
        bb.write(transformationId);
        bb.write_long(cipherText.length);
        bb.write(cipherText);		
	}
}
