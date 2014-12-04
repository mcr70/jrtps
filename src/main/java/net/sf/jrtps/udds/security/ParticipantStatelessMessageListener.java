package net.sf.jrtps.udds.security;

import java.util.List;

import net.sf.jrtps.rtps.Sample;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.udds.Participant;
import net.sf.jrtps.udds.SampleListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ParticipantStatelessMessageListener. This class listens samples from remote 
 * ParticipantStatelessMessageWriter and makes appropriate calls to AuthenticationPlugin 
 * so that authentication and key exchange can be executed.  
 * 
 * @author mcr70
 */
class ParticipantStatelessMessageListener implements SampleListener<ParticipantStatelessMessage> {
	private static final Logger logger = LoggerFactory.getLogger(KeyStoreAuthenticationService.AUTH_LOG_CATEGORY);
	private final Guid participantGuid;
	private final Guid statelessReaderGuid;
	private final KeyStoreAuthenticationService authPlugin;

	ParticipantStatelessMessageListener(Participant p, KeyStoreAuthenticationService authPlugin) {
		this.authPlugin = authPlugin;
		this.participantGuid = p.getGuid();
		this.statelessReaderGuid = new Guid(participantGuid.getPrefix(), EntityId.BUILTIN_PARTICIPANT_STATELESS_READER);
	}
	
	// ----  SampleListener  ------------------------
	@Override
	public void onSamples(List<Sample<ParticipantStatelessMessage>> samples) {
		for (Sample<ParticipantStatelessMessage> sample : samples) {
			ParticipantStatelessMessage psm = sample.getData();

			if (!(psm.destination_participant_key.equals(participantGuid) ||
					psm.destination_participant_key.equals(Guid.GUID_UNKNOWN))) {
				logger.debug("ParticipantStatelessMessage participant_key({}) is not destined to this participant({})",
						psm.destination_participant_key, participantGuid);
				continue;
			}

			if (!(psm.destination_endpoint_key.equals(statelessReaderGuid) ||
					psm.destination_endpoint_key.equals(Guid.GUID_UNKNOWN))) {
				logger.debug("ParticipantStatelessMessage endpoint_key({}) is not destined to local StatelessMessageRader({})",
						psm.destination_endpoint_key, statelessReaderGuid);
				continue;
			}

			logger.debug("Got ParticipantStatelessMessage from {}", psm.source_endpoint_key);
			if (psm.message_data != null && psm.message_data.length > 0) {
				String classId = psm.message_data[0].class_id;
				
				if (HandshakeRequestMessageToken.DDS_AUTH_CHALLENGEREQ_DSA_DH.equals(classId)) {			
					authPlugin.doHandshake(psm.message_identity, 
							(HandshakeRequestMessageToken) psm.message_data[0]);
				}
				else if (HandshakeReplyMessageToken.DDS_AUTH_CHALLENGEREP_DSA_DH.equals(classId)) {
					authPlugin.doHandshake(psm.related_message_identity, 
							(HandshakeReplyMessageToken) psm.message_data[0]);
					
				}
				else if (HandshakeFinalMessageToken.DDS_AUTH_CHALLENGEFIN_DSA_DH.equals(classId)) {
					authPlugin.doHandshake((HandshakeFinalMessageToken) psm.message_data[0]);					
				}
				else {
					logger.warn("HandshakeMessageToken with class_id '{}' not handled", classId);
				}
			}
			else {
				logger.warn("Missing message_data from {}", psm.source_endpoint_key);
			}
		}
	}
}
