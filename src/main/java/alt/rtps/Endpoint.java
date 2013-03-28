package alt.rtps;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
	
	//protected Locator_t unicastLocator;// = new LinkedList<Locator_t>();
	//protected Locator_t multicastLocator;// = new LinkedList<Locator_t>();

	private List<Locator_t> matchedEndpointLocators = new LinkedList<Locator_t>();
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
	
	
	public void addMatchedEndpointLocator(Locator_t locator) {
		matchedEndpointLocators.add(locator);
	}
	
	public List<Locator_t> getMatchedEndpointLocators() {
		return matchedEndpointLocators;
	}


	/**
	 * Gets all locators for given participant.
	 * 
	 * @param prefix GuidPrefix of the participant
	 * @return
	 */
	private Set<Locator_t> getParticipantLocators(GuidPrefix_t prefix) {
		log.trace("getParticipantLocators() for {}: {}", prefix, discoveredParticipants.keySet());
		
		ParticipantData pd = discoveredParticipants.get(prefix);
		if (pd != null) {
			return pd.getAllLocators();
		}
		else {
			log.trace("Unknown participant. Returning default multicast locators");

			HashSet<Locator_t> hs = new HashSet<>();
			hs.add(Locator_t.defaultDiscoveryMulticastLocator(guid.prefix.getDomainId()));
			hs.add(Locator_t.defaultUserMulticastLocator(guid.prefix.getDomainId()));
			
			return hs;
		}
	}


	void setDiscoveredParticipants(HashMap<GuidPrefix_t, ParticipantData> discoveredParticipants) {
		this.discoveredParticipants = discoveredParticipants;
	}
	
	
	protected void sendMessage(Message m, GuidPrefix_t targetPrefix) {
		Set<Locator_t> locators = getParticipantLocators(targetPrefix);

		if (locators.size() > 0) {
			log.debug("Sending message to {}", locators);
		}

		for (Locator_t locator : locators) {
			try {
				UDPWriter w = new UDPWriter(locator); // TODO: No need to create and close all the time
				w.sendMessage(m);
				w.close();					
			} catch (IOException e) {
				log.warn("Failed to send message to {}", locator, e);
			}
		}
	}
}
