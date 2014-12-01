package net.sf.jrtps.udds.security;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;

/**
 * MessageIndetity is used as part of ParticipantStatelessMessage.
 * 
 * @see net.sf.jrtps.udds.security.ParticipantStatelessMessage
 * @author mcr70
 */
public class MessageIdentity {
    private Guid/*BuiltinTopicKey*/ source_guid;
    private long sequence_number; 

    public MessageIdentity(Guid source_guid, long seqNum) {
		this.source_guid = source_guid;
		sequence_number = seqNum;
    }
    
    MessageIdentity(RTPSByteBuffer bb) {
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
