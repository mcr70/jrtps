package net.sf.jrtps.builtin;

import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.udds.security.DataHolder;
import net.sf.jrtps.udds.security.MessageIdentity;
import net.sf.jrtps.udds.security.ParticipantGenericMessage;

/**
 * ParticipantStatelessMessage is defined in DDS Security specification.
 * See 7.4.3.3 Contents of the ParticipantStatelessMessage.
 * 
 * @author mcr70
 */
public class ParticipantStatelessMessage extends ParticipantGenericMessage {
    public static final String GMCLASSID_SECURITY_AUTH_HANDSHAKE = "dds.sec.auth";
    
    public static final String BUILTIN_TOPIC_NAME = "DCPSParticipantStatelessMessage";

	private MessageIdentity message_identity;
	private MessageIdentity related_message_identity;
	private Guid destination_participant_key;
	private Guid destination_endpoint_key;
    private Guid /*BuiltinTopicKey*/ source_endpoint_key;
    private DataHolder[] message_data;
    
	public ParticipantStatelessMessage(MessageIdentity mIdentity, 
			Guid destination_participant_key) {
        this.message_identity = mIdentity;
        this.related_message_identity = new MessageIdentity(Guid.GUID_UNKNOWN, 0);
		this.destination_participant_key = destination_participant_key;
		this.destination_endpoint_key = Guid.GUID_UNKNOWN;
		this.source_endpoint_key = Guid.GUID_UNKNOWN;
		
		message_class_id = GMCLASSID_SECURITY_AUTH_HANDSHAKE;
		message_data = new DataHolder[1];
		message_data[0] = null; // TODO: HandshakeMessageToken
    }
}
