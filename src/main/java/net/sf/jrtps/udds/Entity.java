package net.sf.jrtps.udds;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.builtin.DiscoveredData;
import net.sf.jrtps.types.Guid;

/**
 * Entity is a base class for DataReader and DataWriter.
 * 
 * @author mcr70
 * @param <T> Data type, that this Entity works with
 * @param <COM_LISTENER_TYPE> Type of the communication listener
 */
public class Entity<T, COM_LISTENER_TYPE extends DiscoveredData> {
    private final String topicName;
    private final Participant participant;
    private final Class<T> type;
    private final Guid guid;
    /**
     * A List of communication listeners
     */
    protected final List<CommunicationListener<COM_LISTENER_TYPE>> communicationListeners = new LinkedList<>();
    
    /**
     * Constructor
     * 
     * @param p
     * @param type
     * @param topicName
     *            name of the topic this entity is bound to.
     * @param guid 
     */
    protected Entity(Participant p, Class<T> type, String topicName, Guid guid) {
        this.participant = p;
        this.type = type;
        this.topicName = topicName;
        this.guid = guid;
    }

    /**
     * Adds a new CommunicationListener to this Entity.
     * @param cl
     */
    public void addCommunicationListener(CommunicationListener<COM_LISTENER_TYPE> cl) {
        communicationListeners.add(cl);
    }
    
    /**
     * Removes a CommunicationListener from this Entity.
     * @param cl
     */
    public void removeCommunicationListener(CommunicationListener<COM_LISTENER_TYPE> cl) {
        communicationListeners.remove(cl);
    }
    
    /**
     * Get the name of the topic of this entity.
     * 
     * @return topic name
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * Get the Participant that has created this Entity.
     * 
     * @return Participant
     */
    public Participant getParticipant() {
        return participant;
    }

    /**
     * Gets the type associated with this entity.
     * 
     * @return Class<T>
     */
    public Class<T> getType() {
        return type;
    }
    
    /**
     * Gets the Guid of this Entity
     * @return guid
     */
    public Guid getGuid() {
        return guid;
    }
}
