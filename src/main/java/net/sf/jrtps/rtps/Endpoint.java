package net.sf.jrtps.rtps;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.DiscoveredData;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.message.Message;
import net.sf.jrtps.message.parameter.LocatorParameter;
import net.sf.jrtps.message.parameter.Parameter;
import net.sf.jrtps.transport.Transmitter;
import net.sf.jrtps.transport.TransportProvider;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.types.Locator;
import net.sf.jrtps.udds.Participant;
import net.sf.jrtps.udds.security.CryptoPlugin;
import net.sf.jrtps.udds.security.SecurityException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for RTPSReader and RTPSWriter.
 * 
 * @author mcr70
 */
public class Endpoint {
	private static final Logger logger = LoggerFactory.getLogger(Endpoint.class);

	private final String topicName;
	private final Guid guid;
	private Map<GuidPrefix, ParticipantData> discoveredParticipants;

	private final CryptoPlugin cryptoPlugin;
	private final Configuration configuration;

	private QualityOfService qos;

	private RTPSParticipant participant;

	private final boolean isSecure;

	/**
	 * Constructor 
	 * @param participant {@link RTPSParticipant}
	 * @param entityId {@link EntityId}
	 * @param topicName Name of the topic
	 * @param qos {@link QualityOfService}
	 * @param configuration {@link Configuration}
	 */
	protected Endpoint(RTPSParticipant participant, EntityId entityId, String topicName, QualityOfService qos,
			Configuration configuration) {
		this.participant = participant;
		this.guid = new Guid(participant.getGuid().getPrefix(), entityId);
		this.topicName = topicName;
		this.qos = qos;
		this.configuration = configuration;
		this.cryptoPlugin = participant.getAuthenticationPlugin().getCryptoPlugin();

		// TODO: security should be enabled on topic basis.
		if (entityId.isBuiltinEntity()) { 
			isSecure = false;
		}
		else { 
			isSecure = !"none".equals(configuration.getRTPSProtection());
		}
	}

	/**
	 * Gets the name of the topic associated with this Endpoint.
	 * 
	 * @return name of the topic
	 */
	public String getTopicName() {
		return topicName;
	}

	/**
	 * Gets the Guid of this Endpoint.
	 * 
	 * @return Guid
	 */
	public Guid getGuid() {
		return guid;
	}

	/**
	 * Gets the EntityId of this Endpoint. This is method behaves the same as calling
	 * getGuid().getEntityId().
	 * 
	 * @return EntityId
	 */
	public EntityId getEntityId() {
		return guid.getEntityId();
	}

	Configuration getConfiguration() {
		return configuration;
	}

	void setDiscoveredParticipants(Map<GuidPrefix, ParticipantData> discoveredParticipants) {
		this.discoveredParticipants = discoveredParticipants;
	}

	/**
	 * Gets the QualityOfService associated with this entity.
	 * 
	 * @return QualityOfService
	 */
	public QualityOfService getQualityOfService() {
		return qos;
	}

	/**
	 * Sends a message. If an overflow occurs during marshaling of Message,
	 * only submessages before the overflow will get sent.
	 * 
	 * @param m Message to send
	 * @param proxy proxy of the remote entity
	 * @return true, if an overflow occurred during send.
	 */
	protected boolean sendMessage(Message m, RemoteProxy proxy) {
		if (isSecure) {
			try {
				m = cryptoPlugin.encodeMessage(proxy.getGuid(), m);
			} catch (SecurityException e1) {
				logger.error("Failed to encode message", e1);
				return false;
			}
		}

		boolean overFlowed = false;
		List<Locator> locators = new LinkedList<>();

		if (GuidPrefix.GUIDPREFIX_UNKNOWN.equals(proxy.getGuid().getPrefix())) {
			// GUIDPREFIX_UNKNOWN is used with SPDP; let's send message to every
			// configured locator
			locators.addAll(proxy.getLocators());
		}
		else {
			locators.add(proxy.getLocator());
		}

		for (Locator locator : locators) {
			logger.debug("Sending message to {}", locator);

			if (locator != null) {
				try {
					TransportProvider provider = TransportProvider.getInstance(locator);
					Transmitter tr = provider.getTransmitter(locator);

					overFlowed = tr.sendMessage(m);
				} catch (IOException e) {
					logger.warn("[{}] Failed to send message to {}", getGuid().getEntityId(), locator, e);
				}
			} 
			else {
				logger.debug("[{}] Unable to send message, no suitable locator for proxy {}", getGuid().getEntityId(), proxy);
				// participant.ignoreParticipant(targetPrefix);
			}
		}

		return overFlowed;
	}

	/**
	 * Get the RTPSParticipant, that created this entity.
	 * 
	 * @return RTPSParticipant
	 */
	protected RTPSParticipant getParticipant() {
		return participant;
	}

	/**
	 * Checks, if this endpoint is secure or not. If an endpoint is secure,
	 * every message that is being sent, will be encoded by Transformer.
	 * 
	 * @return true or false
	 */
	public boolean isSecure() {
		return isSecure;
	}

	/**
	 * Gets locators for given remote Guid
	 * 
	 * @param dd
	 * @return unicast and multicast locator
	 */
	List<Locator> getLocators(DiscoveredData dd) {
		List<Locator> locators = new LinkedList<>();

		// check if proxys discovery data contains locator info
		if (!(dd instanceof ParticipantData)) {
			List<Parameter> params = dd.getParameters();
			for (Parameter p : params) {
				if (p instanceof LocatorParameter) {
					LocatorParameter locParam = (LocatorParameter) p;
					locators.add(locParam.getLocator());
				} 
			}
		}


		Guid remoteGuid = dd.getBuiltinTopicKey();

		// Set the default locators from ParticipantData
		ParticipantData pd = discoveredParticipants.get(remoteGuid.getPrefix());
		if (pd != null) {
			if (remoteGuid.getEntityId().isBuiltinEntity()) {
				locators.addAll(pd.getDiscoveryLocators());
			} 
			else {
				locators.addAll(pd.getUserdataLocators());
			}
		}
		else {
			logger.warn("ParticipantData was not found for {}, cannot set default locators", remoteGuid);
		}

		return locators;
	}
}
