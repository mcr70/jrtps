package net.sf.jrtps.udds.security;

import java.security.cert.CertificateException;

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

	ParticipantStatelessMessage(RTPSByteBuffer bb) throws Exception {
		super(bb);
	}

	ParticipantStatelessMessage(MessageIdentity messageIdentity, 
			DataHolder dHolder) {
		this(messageIdentity, Guid.GUID_UNKNOWN, Guid.GUID_UNKNOWN, messageIdentity.getSourceGuid(), dHolder);
	}
	
	private ParticipantStatelessMessage(MessageIdentity messageIdentity, 
			Guid remoteParticipant, Guid destEndpoint, Guid sourceEndpoint,
			DataHolder dHolder) {
		super(messageIdentity, new MessageIdentity(Guid.GUID_UNKNOWN, 0), 
				remoteParticipant, destEndpoint, sourceEndpoint, 
				GMCLASSID_SECURITY_AUTH_HANDSHAKE, new DataHolder[] {dHolder});
	}

	Guid getSourceGuid() {
		return message_identity.getSourceGuid();
	}
	
	@Override
	DataHolder readMessageData(RTPSByteBuffer bb) throws CertificateException  {
		String class_id = bb.read_string();
		
		if (class_id.equals(HandshakeRequestMessageToken.DDS_AUTH_CHALLENGEREQ_DSA_DH) ||
				class_id.equals(HandshakeRequestMessageToken.DDS_AUTH_CHALLENGEREQ_PKI_RSA)	) {
				
			return new HandshakeRequestMessageToken(class_id, bb);
		}
		else if (class_id.equals(HandshakeReplyMessageToken.DDS_AUTH_CHALLENGEREP_DSA_DH) ||
				class_id.equals(HandshakeReplyMessageToken.DDS_AUTH_CHALLENGEREP_PKI_RSA)	) {
				
			return new HandshakeReplyMessageToken(class_id, bb);
		}
		else if (class_id.equals(HandshakeFinalMessageToken.DDS_AUTH_CHALLENGEFIN_DSA_DH) ||
				class_id.equals(HandshakeFinalMessageToken.DDS_AUTH_CHALLENGEFIN_PKI_RSA)	) {
				
			return new HandshakeFinalMessageToken(class_id, bb);
		}

		logger.warn("Unknown class_id {}", class_id);
		return null;
	}

	@Override
	void writeMessageData(DataHolder dh, RTPSByteBuffer bb) {
		bb.write_string(dh.class_id);
		dh.writeTo(bb);
	}
}
