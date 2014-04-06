package net.sf.jrtps.builtin;

import net.sf.jrtps.types.Guid;

/**
 * Used with DDS security.
 * @author mcr70
 */
class InterParticipantGenericMessage {
    MessageIdentity message_identity;
    MessageIdentity related_message_identity;
    Guid destination_participant_key;
    Guid destination_endpoint_key;
    Guid source_endpoint_key;
    
    GenericMessageClassId message_class_id;
    DataHolderSeq message_data;
    
    static class MessageIdentity {
        Guid  source_guid;
        long sequence_number;
    }
    
    static class Property {
        
    }
    static class BinaryProperty {
        
    }
    
    static class DataHolder {
        String class_id;
        Property[] string_properties;
        BinaryProperty[] binary_properties;
        String[] string_values;
        byte[] binary_value1;
        byte[] binary_value2;
        long[] longlong_values;
    }
    
    static class DataHolderSeq {    
    }
    
    static class GenericMessageClassId {
        static final String GMCLASSID_SECURITY_AUTH_HANDSHAKE = "dds.sec.auth";
        static final String GMCLASSID_SECURITY_PARTICIPANT_CRYPTO_TOKENS = "dds.sec.participant_crypto_tokens";
        static final String GMCLASSID_SECURITY_DATAWRITER_CRYPTO_TOKENS = "dds.sec.writer_crypto_tokens";
        static final String GMCLASSID_SECURITY_DATAREADER_CRYPTO_TOKENS = "dds.sec.reader_crypto_tokens";
    }
}

