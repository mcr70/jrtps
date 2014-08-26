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
        logger.debug("Sending message to {}", locator);

        if (locator != null) {
            try {
                TransportProvider handler = TransportProvider.getInstance(locator);
                Transmitter tr = handler.createTransmitter(locator, configuration.getBufferSize());
                // TODO: No need to create and close all the time

                overFlowed = tr.sendMessage(m);
                tr.close();
            } catch (IOException e) {
                logger.warn("[{}] Failed to send message to {}", getGuid().getEntityId(), locator, e);
            }
        } else {
            logger.debug("[{}] Unable to send message, no suitable locator for proxy {}", getGuid().getEntityId(), proxy);
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
        if (pd == null) {
            logger.debug("PD was null for {}, {}", remoteGuid.getPrefix(), discoveredParticipants.keySet());
        }
        
        if (remoteGuid.getEntityId().isBuiltinEntity()) {
            locators.addAll(pd.getDiscoveryLocators());
        } 
        else {
            locators.addAll(pd.getUserdataLocators());
        }

        return locators;
    }
}
