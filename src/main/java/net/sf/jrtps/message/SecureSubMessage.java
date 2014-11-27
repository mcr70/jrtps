package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * SecureSubMessage is used to wrap one or more RTPS submessages.
 * Contents of wrapped submessages are secured as specified by 
 * transformationKind and transformationId.
 * 
 * @author mcr70
 */
class SecureSubMessage extends SubMessage {
    public static final int KIND = 0x30;
    
    private int transformationKind; // long
    private byte[] trasformationId; // octet[8]
    private byte[] cipherText;      // octet[*]
    
    SecureSubMessage() { // TODO: implement public constructor
        super(new SubMessageHeader(KIND));
    }

    SecureSubMessage(SubMessageHeader smh, RTPSByteBuffer bb) {
        super(new SubMessageHeader(KIND));
        
        transformationKind = bb.read_long();
        trasformationId = new byte[8];
        bb.read(trasformationId);
        
        cipherText = new byte[bb.read_long()];
        bb.read(cipherText);
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

    /**
     * Gets the TransformationKind of the SecuredPayload.
     * @return transformationKind
     */
    public int getTransformationKind() {
        return transformationKind;
    }
    
    /**
     * Gets the transformationId.
     * @return transformationId
     */
    public byte[] getTransformationId() {
        return trasformationId;
    }
    
    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(transformationKind);
        bb.write(trasformationId);
        bb.write_long(cipherText.length);
        bb.write(cipherText);
    }
}
