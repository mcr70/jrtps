package net.sf.jrtps.udds.security;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;

class MessageIdentity {
    private Guid/*BuiltinTopicKey*/ source_guid;
    private long sequence_number; 

    public MessageIdentity(RTPSByteBuffer bb) {
        Guid guid = new Guid(bb);
        sequence_number = bb.read_longlong();
    }
    
    public Guid getSourceGuid() {
        return source_guid;
    }
    
    public long getSequenceNumber() {
        return sequence_number;
    }
    
    void writeTo(RTPSByteBuffer bb) {
        source_guid.writeTo(bb);
        bb.write_longlong(sequence_number);
    }
}
