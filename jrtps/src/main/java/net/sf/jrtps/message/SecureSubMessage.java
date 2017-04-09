package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.udds.security.SecurePayload;

/**
 * SecureSubMessage is used to wrap one or more RTPS submessages.
 * Contents of wrapped submessages are secured as specified by 
 * transformationKind and transformationId.
 * 
 * @author mcr70
 */
public class SecureSubMessage extends SubMessage {
    public static final int KIND = 0x30;
    
	private SecurePayload payload;
    
    public SecureSubMessage(SecurePayload payload) {
        super(new SubMessageHeader(KIND));
    	this.payload = payload;
    }

    SecureSubMessage(SubMessageHeader smh, RTPSByteBuffer bb) {
        super(smh);
        
        int transformationKind = bb.read_long();
        byte[] trasformationId = new byte[8];
        bb.read(trasformationId);
        
        byte[] cipherText = new byte[bb.read_long()];
        bb.read(cipherText);
        
        this.payload = new SecurePayload(transformationKind, trasformationId, cipherText);
    }
    

	/**
     * Gets the value of singleSubMessageFlag. If this flag is set, SecureSubMessage
     * is an envelope for a single RTPS submessage. Otherwise, SecureSubMessage
     * is an envelope for a full RTPS message.
     * 
     * @return true, if only one submessage is encapsulated in SecuredPayload
     */
    public boolean singleSubMessageFlag() {
        return (header.flags & 0x2) != 0;
    }

    public SecurePayload getSecurePayload() {
    	return payload;
    }
    
    @Override
    public void writeTo(RTPSByteBuffer bb) {
    	payload.writeTo(bb);
    }

    /**
     * Sets or resets the value of SingleSubMessage flag.
     * @param s whether to set or reset.
     */
	public void singleSubMessageFlag(boolean s) {
		if (s) {
			header.flags |= 0x2;
		}
		else {
			header.flags &= ~0x2;			
		}
	}
}
