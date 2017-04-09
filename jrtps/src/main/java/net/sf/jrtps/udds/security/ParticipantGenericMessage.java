package net.sf.jrtps.udds.security;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;

abstract class ParticipantGenericMessage {
    MessageIdentity message_identity;
    MessageIdentity related_message_identity;
    Guid /*BuiltinTopicKey*/ destination_participant_key;
    Guid /*BuiltinTopicKey*/ destination_endpoint_key;
    Guid /*BuiltinTopicKey*/ source_endpoint_key;
    
    protected String message_class_id;
    DataHolder[] message_data;
    
    public ParticipantGenericMessage(MessageIdentity mi, MessageIdentity related_mi,
    		Guid destParticipantKey, Guid destEndpointKey, Guid sourceEndpointKey,
    		String mClassId, DataHolder[] message_data) {
				message_identity = mi;
				related_message_identity = related_mi;
				destination_participant_key = destParticipantKey;
				destination_endpoint_key = destEndpointKey;
				source_endpoint_key = sourceEndpointKey;
				message_class_id = mClassId;
				this.message_data = message_data;  	
    }
    
    public ParticipantGenericMessage(RTPSByteBuffer bb) throws Exception {
        message_identity = new MessageIdentity(bb);
        related_message_identity = new MessageIdentity(bb);
        destination_participant_key = new Guid(bb);
        destination_endpoint_key = new Guid(bb);
        source_endpoint_key = new Guid(bb);
        
        message_class_id = bb.read_string();
        int count = bb.read_long();
        message_data = new DataHolder[count];
        for (int i = 0; i < count; i++) {
        	message_data[i] = readMessageData(bb);
        }
	}

    public void writeTo(RTPSByteBuffer bb) {
    	message_identity.writeTo(bb);
    	related_message_identity.writeTo(bb);
    	destination_participant_key.writeTo(bb);
    	destination_endpoint_key.writeTo(bb);
    	source_endpoint_key.writeTo(bb);
    	bb.write_string(message_class_id);
    	bb.write_long(message_data.length);
    	for (DataHolder dh : message_data) {
    		writeMessageData(dh, bb);
    	}
    }

	abstract void writeMessageData(DataHolder dh, RTPSByteBuffer bb);

	abstract DataHolder readMessageData(RTPSByteBuffer bb) throws Exception;
}
