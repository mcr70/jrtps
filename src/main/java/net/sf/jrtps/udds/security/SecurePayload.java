package net.sf.jrtps.udds.security;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class SecurePayload {
	private int transformationKind; // long, one of predefined integers above
	private byte[] transformationId; // octet[8]
	private byte[] cipherText;      // octet[*]

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
	
	public int getTransformationKind() {
		return transformationKind;
	}
	
	public byte[] getTrasformationId() {
		return transformationId;
	}
	
	public byte[] getCipherText() {
		return cipherText;
	}

	public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(transformationKind);
        bb.write(transformationId);
        bb.write_long(cipherText.length);
        bb.write(cipherText);		
	}
}
