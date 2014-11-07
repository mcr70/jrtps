package net.sf.jrtps.udds.security;

import net.sf.jrtps.message.parameter.BuiltinTopicKey;

public class ParticipantGenericMessage {
    MessageIdentity message_identity;
    MessageIdentity related_message_identity;
    BuiltinTopicKey destination_participant_key;
    BuiltinTopicKey destination_endpoint_key;
    BuiltinTopicKey source_endpoint_key;
    
    String message_class_id;
    DataHolder[] message_data;
}
