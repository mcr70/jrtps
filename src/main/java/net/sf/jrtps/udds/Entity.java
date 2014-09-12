package net.sf.jrtps.udds;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.builtin.DiscoveredData;
import net.sf.jrtps.types.Guid;

/**
 * Entity is a base class for DataReader and DataWriter.
 * 
 * @author mcr70
 * @param <T>
 * 
 */
public class Entity<T, COMMTYPE extends DiscoveredData> {
    private final String topicName;
    private final Participant participant;
    private final Class<T> type;
    private final Guid guid;
    private final List<CommunicationListener<COMMTYPE>> communicationListeners = new LinkedList<>();
    
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
    void addCommunicationListener(CommunicationListener<COMMTYPE> cl) {
        communicationListeners.add(cl);
    }
    
    /**
     * Removes a CommunicationListener from this Entity.
     * @param cl
     */
    void removeCommunicationListener(CommunicationListener<COMMTYPE> cl) {
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
