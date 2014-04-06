package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;


class SecureSubMessage extends SubMessage {
    public static final int KIND = 0x30;
    
    private int transformationKind; // long
    private byte[] trasformationId; // octet[8]
    private byte[] payload;         // octet[*]
    
    SecureSubMessage() {
        super(new SubMessageHeader(KIND));
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        // TODO Auto-generated method stub
        
    }
}
