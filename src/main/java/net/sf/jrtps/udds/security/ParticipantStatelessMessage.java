package net.sf.jrtps.udds.security;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ParticipantStatelessMessage is defined in DDS Security specification.
 * See 7.4.3.3 Contents of the ParticipantStatelessMessage.
 * 
 * @author mcr70
 */
public class ParticipantStatelessMessage extends ParticipantGenericMessage {
	private static final Logger logger = LoggerFactory.getLogger(ParticipantStatelessMessage.class);
	
	public static final String GMCLASSID_SECURITY_AUTH_HANDSHAKE = "dds.sec.auth";    
    public static final String BUILTIN_TOPIC_NAME = "DCPSParticipantStatelessMessage";

	ParticipantStatelessMessage(RTPSByteBuffer bb) {
		super(bb);
	}

	ParticipantStatelessMessage(MessageIdentity messageIdentity, 
			HandshakeRequestMessageToken hrmt) {
		this(messageIdentity, Guid.GUID_UNKNOWN, Guid.GUID_UNKNOWN, Guid.GUID_UNKNOWN, hrmt);
	}
	
	ParticipantStatelessMessage(MessageIdentity messageIdentity, 
			Guid remoteParticipant, Guid destEndpoint, Guid sourceEndpoint,
			HandshakeRequestMessageToken hrmt) {
		super(messageIdentity, new MessageIdentity(Guid.GUID_UNKNOWN, 0), 
				remoteParticipant, destEndpoint, sourceEndpoint, 
				GMCLASSID_SECURITY_AUTH_HANDSHAKE, new DataHolder[] {hrmt});
	}

	@Override
	DataHolder readMessageData(RTPSByteBuffer bb) {
		String class_id = bb.read_string();
		
		if (class_id.equals(HandshakeRequestMessageToken.DDS_AUTH_CHALLENGEREQ_DSA_DH) ||
				class_id.equals(HandshakeRequestMessageToken.DDS_AUTH_CHALLENGEREQ_PKI_RSA)	) {
			return new HandshakeRequestMessageToken(class_id, bb);
		}

		logger.warn("Unknown class_id {}", class_id);
		return null;
	}

	@Override
	void writeMessageData(DataHolder dh, RTPSByteBuffer bb) {
		bb.write_string(dh.class_id);
		
		// TODO: writeTo for messageToken
	}
	
}
