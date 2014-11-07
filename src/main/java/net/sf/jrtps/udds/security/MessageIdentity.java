package net.sf.jrtps.udds.security;

import net.sf.jrtps.message.parameter.BuiltinTopicKey;

class MessageIdentity {
    BuiltinTopicKey source_guid;
    long sequence_number; 
}
