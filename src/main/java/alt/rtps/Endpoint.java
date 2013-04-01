package alt.rtps;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.builtin.ParticipantData;
import alt.rtps.message.Message;
import alt.rtps.transport.UDPWriter;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.GUID_t;
import alt.rtps.types.GuidPrefix_t;
import alt.rtps.types.Locator_t;

public class Endpoint {
	private static final Logger log = LoggerFactory.getLogger(Endpoint.class);

	private final String topicName;
	private final GUID_t guid;	
	private HashMap<GuidPrefix_t, ParticipantData> discoveredParticipants;


	/**
	 * 
	 * @param prefix prefix from the participant that creates this endpoint.
	 * @param entityId
	 */
	protected Endpoint(GuidPrefix_t prefix, EntityId_t entityId, String topicName) {
		this.guid = new GUID_t(prefix, entityId);

		this.topicName = topicName;
	}


	public String getTopicName() {
		return topicName;
	}

	public GUID_t getGuid() {
		return guid;
	}


	/**
	 * Gets all locators for given participant.
	 * 
	 * @param prefix GuidPrefix of the participant
	 * @return
	 */
	private Locator_t getParticipantLocators(GuidPrefix_t prefix) {
		log.trace("[{}] getParticipantLocators() for {}: {}", getGuid().entityId, prefix, discoveredParticipants.keySet());
		
		ParticipantData pd = discoveredParticipants.get(prefix);
		if (pd != null) {
			if (guid.entityId.isBuiltinEntity()) {
				return pd.getMetatrafficUnicastLocator();
			}
			else {
				return pd.getUnicastLocator();
			}
		}

		log.debug("[{}] Unknown participant. Returning default metatraffic multicast locator", getGuid().entityId);
		return Locator_t.defaultDiscoveryMulticastLocator(guid.prefix.getDomainId());
	}


	void setDiscoveredParticipants(HashMap<GuidPrefix_t, ParticipantData> discoveredParticipants) {
		this.discoveredParticipants = discoveredParticipants;
	}


	protected void sendMessage(Message m, GuidPrefix_t targetPrefix) {
		// TODO: we should check, that there is a recipient we need in each Locator.
		//       now we just assume remote participant will ignore if there isn't
		// we should have sendMessage(Messagem, GuidPrefix_t remoteParticipant, EntityId_t remoteEntity)
		// - RTPSReader.onHeartbeat : sendMessage(m, senderGuidPrefix, hb.getWriterId())
		// - RTPSWriter.sendData : sendMessage(m, senderPrefix, ackNack.getReaderId())
		// - RTPSWriter.sendHeartbeat : sendMessage(m, senderPrefix, ackNack.getReaderId())
		// - Writer.setResendDataPeriod : sendMessage(m, senderPrefix, null) // or new EntityId::Participant();
		//
		// getParticipantLocators should be changed to getLocator(new Guid(prefix, entityId))
		// Q: prefer multicast?
		Locator_t locator = getParticipantLocators(targetPrefix);

		try {
			UDPWriter w = new UDPWriter(locator); // TODO: No need to create and close all the time
			w.sendMessage(m);
			w.close();					
		} catch (IOException e) {
			log.warn("[{}] Failed to send message to {}", getGuid().entityId, locator, e);
		}
	}
}
