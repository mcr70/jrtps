package net.sf.jrtps.udds.security;

import java.security.cert.CertificateException;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ParticipantVolatileSecureMessage is defined in DDS Security specification.
 * See 7.4.4.3 Contents of the ParticipantVolatileSecureMessage.
 * 
 * @author mcr70
 */
class ParticipantVolatileSecureMessage extends ParticipantGenericMessage {
	private static final Logger logger = LoggerFactory.getLogger(ParticipantVolatileSecureMessage.class);
	
	public static final String GMCLASSID_SECURITY_PARTICIPANT_CRYPTO_TOKENS = "dds.sec.participant_crypto_tokens";
	public static final String GMCLASSID_SECURITY_DATAWRITER_CRYPTO_TOKENS = "dds.sec.datawriter_crypto_tokens";
	public static final String GMCLASSID_SECURITY_DATAREADER_CRYPTO_TOKENS = "dds.sec.datareader_crypto_tokens";
	
    public static final String BUILTIN_TOPIC_NAME = "DCPSParticipantVolatileSecureMessage";

    enum CryptoTokensKind {
    	PARTICIPANT(GMCLASSID_SECURITY_PARTICIPANT_CRYPTO_TOKENS), 
    	DATAWRITER(GMCLASSID_SECURITY_DATAWRITER_CRYPTO_TOKENS), 
    	DATAREADER(GMCLASSID_SECURITY_DATAREADER_CRYPTO_TOKENS);
    	
    	private String value;
    	
    	private CryptoTokensKind(String s) {
    		this.value = s;
    	}
    }
    
	ParticipantVolatileSecureMessage(RTPSByteBuffer bb) throws Exception {
		super(bb);
	}

	ParticipantVolatileSecureMessage(CryptoTokensKind kind, MessageIdentity messageIdentity, 
			DataHolder dHolder) {
		this(kind, messageIdentity, Guid.GUID_UNKNOWN, Guid.GUID_UNKNOWN, messageIdentity.getSourceGuid(), dHolder);
	}
	
	private ParticipantVolatileSecureMessage(CryptoTokensKind kind, MessageIdentity messageIdentity, 
			Guid remoteParticipant, Guid destEndpoint, Guid sourceEndpoint,
			DataHolder dHolder) {
		super(messageIdentity, new MessageIdentity(Guid.GUID_UNKNOWN, 0), 
				remoteParticipant, destEndpoint, sourceEndpoint, 
				kind.value, new DataHolder[] {dHolder});
	}

	
	@Override
	DataHolder readMessageData(RTPSByteBuffer bb) throws CertificateException  {
		// TODO: implement me
		return null;
	}

	@Override
	void writeMessageData(DataHolder dh, RTPSByteBuffer bb) {
		// TODO: implement me
	}
}
