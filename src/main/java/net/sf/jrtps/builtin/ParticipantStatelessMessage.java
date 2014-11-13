package net.sf.jrtps.builtin;

import net.sf.jrtps.types.Guid;
import net.sf.jrtps.udds.security.ParticipantGenericMessage;

/**
 * ParticipantStatelessMessage is defined in DDS Security specification.
 * @author mcr70
 */
public class ParticipantStatelessMessage extends ParticipantGenericMessage {
    public static final String GMCLASSID_SECURITY_AUTH_HANDSHAKE = "dds.sec.auth";
    
    public static final String BUILTIN_TOPIC_NAME = "DCPSParticipantStatelessMessage";


	public ParticipantStatelessMessage(Guid source_guid, Guid target_guid) {
        message_class_id = GMCLASSID_SECURITY_AUTH_HANDSHAKE;
    }
}
