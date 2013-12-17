package net.sf.jrtps;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.HashMap;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.message.Message;
import net.sf.jrtps.transport.UDPWriter;
import net.sf.jrtps.types.EntityId_t;
import net.sf.jrtps.types.GUID_t;
import net.sf.jrtps.types.GuidPrefix_t;
import net.sf.jrtps.types.Locator_t;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Endpoint {
	private static final Logger log = LoggerFactory.getLogger(Endpoint.class);

	private final String topicName;
	private final GUID_t guid;	
	private HashMap<GuidPrefix_t, ParticipantData> discoveredParticipants;

	private final Configuration configuration;

	private QualityOfService qos;

	private RTPSParticipant participant;


	/**
	 * 
	 * @param participant 
	 * @param prefix prefix from the participant that creates this endpoint.
	 * @param entityId
	 * @param topicName
	 * @param qos 
	 */
	protected Endpoint(RTPSParticipant participant, EntityId_t entityId, String topicName, QualityOfService qos, Configuration configuration) {
		this.participant = participant;
		this.guid = new GUID_t(participant.getGuid().prefix, entityId);
		this.topicName = topicName;
		this.qos = qos;
		this.configuration = configuration;
	}


	public String getTopicName() {
		return topicName;
	}

	public GUID_t getGuid() {
		return guid;
	}

	Configuration getConfiguration() {
		return configuration;
	}
	
	/**
	 * Gets all locators for given participant.
	 * 
	 * @param prefix GuidPrefix of the participant
	 * @return Locator_t
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

		log.trace("[{}] Unknown participant. Returning default metatraffic multicast locator for domain {}", getGuid().entityId, participant.getDomainId());

		return Locator_t.defaultDiscoveryMulticastLocator(participant.getDomainId());
	}


	void setDiscoveredParticipants(HashMap<GuidPrefix_t, ParticipantData> discoveredParticipants) {
		this.discoveredParticipants = discoveredParticipants;
	}

	/**
	 * Gets the QualityOfService associated with this entity.
	 * @return QualityOfService
	 */
	public QualityOfService getQualityOfService() {
		return qos;
	}

	protected boolean sendMessage(Message m, GuidPrefix_t targetPrefix) {
		Locator_t locator = getParticipantLocators(targetPrefix);
		boolean overFlowed = true;
		try {
			UDPWriter w = new UDPWriter(locator, configuration.getBufferSize()); // TODO: No need to create and close all the time
			overFlowed = w.sendMessage(m);
			w.close();					
		} 
		catch(IOException e) {
			log.warn("[{}] Failed to send message to {}", getGuid().entityId, locator, e);
		}
		catch(BufferOverflowException boe) {
			log.warn("Got unexpected BufferOverflowException, buffer size is {}. It should be increased.", configuration.getBufferSize());
		}

		return overFlowed;
	}

	/**
	 * Get the RTPSParticipant, that created this entity. 
	 * @return RTPSParticipant
	 */
	protected RTPSParticipant getParticipant() {
		return participant;
	}
}
