package net.sf.jrtps;

import java.io.IOException;
import java.nio.BufferOverflowException;
import java.util.Map;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.message.Message;
import net.sf.jrtps.transport.UDPWriter;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Endpoint {
	private static final Logger log = LoggerFactory.getLogger(Endpoint.class);

	private final String topicName;
	private final Guid guid;	
	private Map<GuidPrefix, ParticipantData> discoveredParticipants;

	private final Configuration configuration;

	private QualityOfService qos;

	private RTPSParticipant participant;


	/**
	 * 
	 * @param participant 
	 * @param entityId
	 * @param topicName
	 * @param qos 
	 * @param configuration
	 */
	protected Endpoint(RTPSParticipant participant, EntityId entityId, String topicName, QualityOfService qos, Configuration configuration) {
		this.participant = participant;
		this.guid = new Guid(participant.getGuid().getPrefix(), entityId);
		this.topicName = topicName;
		this.qos = qos;
		this.configuration = configuration;
	}

	/**
	 * Gets the name of the topic associated with this Endpoint.
	 * @return name of the topic
	 */
	public String getTopicName() {
		return topicName;
	}

	/**
	 * Gets the Guid of this Endpoint.
	 * @return Guid
	 */
	public Guid getGuid() {
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
	private Locator getParticipantLocators(GuidPrefix prefix) {
		log.trace("[{}] getParticipantLocators() for {}: {}", getGuid().getEntityId(), prefix, discoveredParticipants.keySet());
		
		if (prefix == null || GuidPrefix.GUIDPREFIX_UNKNOWN.equals(prefix)) {
			return Locator.defaultDiscoveryMulticastLocator(participant.getDomainId());			
		}
		
		ParticipantData pd = discoveredParticipants.get(prefix);
		if (pd != null) {
			if (guid.getEntityId().isBuiltinEntity()) {
				return pd.getMetatrafficUnicastLocator();
			}
			else {
				return pd.getUnicastLocator();
			}
		}

		log.warn("[{}] Unknown participant {}. Returning default metatraffic multicast locator for domain {}", getGuid().getEntityId(), prefix, participant.getDomainId());

		return Locator.defaultDiscoveryMulticastLocator(participant.getDomainId());
	}


	void setDiscoveredParticipants(Map<GuidPrefix, ParticipantData> discoveredParticipants) {
		this.discoveredParticipants = discoveredParticipants;
	}

	/**
	 * Gets the QualityOfService associated with this entity.
	 * @return QualityOfService
	 */
	public QualityOfService getQualityOfService() {
		return qos;
	}

	/**
	 * Sends a message. If an overflow occurs during marshalling of Message, only submessages before 
	 * the overflow will get sent. 
	 * 
	 * @param m Message to send
	 * @param targetPrefix GuidPrefix of the target participant
	 * @return true, if an overflow occured during send.
	 */
	protected boolean sendMessage(Message m, GuidPrefix targetPrefix) {
		Locator locator = getParticipantLocators(targetPrefix);
		boolean overFlowed = true;
		try {
			UDPWriter w = new UDPWriter(locator, configuration.getBufferSize()); // TODO: No need to create and close all the time
			overFlowed = w.sendMessage(m);
			w.close();					
		} 
		catch(IOException e) {
			log.warn("[{}] Failed to send message to {}", getGuid().getEntityId(), locator, e);
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
