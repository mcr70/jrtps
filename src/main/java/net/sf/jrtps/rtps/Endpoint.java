package net.sf.jrtps.rtps;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.DiscoveredData;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.message.Message;
import net.sf.jrtps.message.parameter.MulticastLocator;
import net.sf.jrtps.message.parameter.Parameter;
import net.sf.jrtps.message.parameter.UnicastLocator;
import net.sf.jrtps.transport.TransportProvider;
import net.sf.jrtps.transport.Writer;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for RTPSReader and RTPSWriter.
 * 
 * @author mcr70
 */
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
    protected Endpoint(RTPSParticipant participant, EntityId entityId, String topicName, QualityOfService qos,
            Configuration configuration) {
        this.participant = participant;
        this.guid = new Guid(participant.getGuid().getPrefix(), entityId);
        this.topicName = topicName;
        this.qos = qos;
        this.configuration = configuration;
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
     * @param m
     *            Message to send
     * @param proxy
     *            proxy of the remote entity
     * @return true, if an overflow occurred during send.
     */
    protected boolean sendMessage(Message m, RemoteProxy proxy) {
        boolean overFlowed = false;
        
        Locator locator = proxy.getLocator();
        log.debug("Sending message to {}", locator);

        if (locator != null) {
            try {
                TransportProvider handler = TransportProvider.getInstance(locator);
                Writer w = handler.createWriter(locator, configuration.getBufferSize());
                //UDPWriter w = new UDPWriter(locator, configuration.getBufferSize()); 
                // TODO: No need to create and close all the time

                overFlowed = w.sendMessage(m);
                w.close();
            } catch (IOException e) {
                log.warn("[{}] Failed to send message to {}", getGuid().getEntityId(), locator, e);
            }
        } else {
            log.debug("[{}] Unable to send message, no locator for proxy {}", getGuid().getEntityId(), proxy);
            // participant.ignoreParticipant(targetPrefix);
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
     * Gets locators for given remote Guid
     * 
     * @param dd
     * @return unicast and multicast locator
     */
    LocatorPair getLocators(DiscoveredData dd) {
        Guid remoteGuid = dd.getKey();
        LocatorPair locators = new LocatorPair();

        // Set the default locators from ParticipantData
        ParticipantData pd = discoveredParticipants.get(remoteGuid.getPrefix());

        if (remoteGuid.getEntityId().isBuiltinEntity()) {
            locators.ucLocator = pd.getMetatrafficUnicastLocator();
            locators.mcLocator = pd.getMetatrafficMulticastLocator();
        } else {
            locators.ucLocator = pd.getUnicastLocator();
            locators.mcLocator = pd.getMulticastLocator();
        }

        // Then check if proxys discovery data contains locator info
        List<Parameter> params = dd.getParameters();
        for (Parameter p : params) {
            if (p instanceof UnicastLocator) {
                UnicastLocator ul = (UnicastLocator) p;
                locators.ucLocator = ul.getLocator();
            } else if (p instanceof MulticastLocator) {
                MulticastLocator mc = (MulticastLocator) p;
                locators.mcLocator = mc.getLocator();
            }
        }

        return locators;
    }
}
