package net.sf.jrtps.udds.security;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;

/**
 * MessageIndetity is used as part of ParticipantStatelessMessage.
 * 
 * @see net.sf.jrtps.udds.security.ParticipantStatelessMessage
 * @author mcr70
 */
class MessageIdentity {
    private Guid/*BuiltinTopicKey*/ source_guid;
    private long sequence_number; 

    MessageIdentity(Guid source_guid, long seqNum) {
		this.source_guid = source_guid;
		sequence_number = seqNum;
    }
    
    MessageIdentity(RTPSByteBuffer bb) {
        Guid guid = new Guid(bb);
        sequence_number = bb.read_longlong();
    }
    
    /**
     * Gets the source Guid
     * @return Guid
     */
    Guid getSourceGuid() {
        return source_guid;
    }
    
    /**
     * Gets the sequence number of this MessageIdentity
     * @return sequence number
     */
    long getSequenceNumber() {
        return sequence_number;
    }
    
    void writeTo(RTPSByteBuffer bb) {
        source_guid.writeTo(bb);
        bb.write_longlong(sequence_number);
    }
}
