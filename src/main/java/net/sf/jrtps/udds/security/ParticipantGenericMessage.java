package net.sf.jrtps.udds.security;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;

public class ParticipantGenericMessage {
    MessageIdentity message_identity;
    MessageIdentity related_message_identity;
    Guid /*BuiltinTopicKey*/ destination_participant_key;
    Guid /*BuiltinTopicKey*/ destination_endpoint_key;
    Guid /*BuiltinTopicKey*/ source_endpoint_key;
    
    protected String message_class_id;
    DataHolder[] message_data;
    
    
    public void readFrom(RTPSByteBuffer bb) {
        message_identity = new MessageIdentity(bb);
        related_message_identity = new MessageIdentity(bb);
        destination_participant_key = new Guid(bb);
        destination_endpoint_key = new Guid(bb);
        source_endpoint_key = new Guid(bb);
        
        message_class_id = bb.read_string();
        int count = bb.read_long();
        message_data = new DataHolder[count];
        for (int i = 0; i < count; i++) {
            // TODO: ....
        }
    }
}
