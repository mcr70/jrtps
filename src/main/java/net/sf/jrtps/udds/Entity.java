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
 * @param <ENTITY_DATA> Type of the remote entity data, that is tracked by communication listeners of this Entity.
 */
public class Entity<T, ENTITY_DATA extends DiscoveredData> {
    private final String topicName;
    private final Participant participant;
    private final Class<T> type;
    private final String typeName;
    private final Guid guid;
    /**
     * A List of communication listeners
     */
    protected final List<CommunicationListener<ENTITY_DATA>> communicationListeners = new LinkedList<>();
    
    /**
     * Constructor
     * 
     * @param p Participant
     * @param type Type of the entity
     * @param typeName if type null, typeName is set to be fully qualified class name of type 
     * @param topicName name of the topic this entity is bound to.
     * @param guid Guid of this Entity
     */
    protected Entity(Participant p, Class<T> type, String typeName, String topicName, Guid guid) {
        this.participant = p;
        this.type = type;
        if (typeName != null) {
        	this.typeName = typeName;
        }
        else {
        	this.typeName = type.getName();
        }
        this.topicName = topicName;
        this.guid = guid;
    }

    /**
     * Adds a new CommunicationListener to this Entity.
     * @param cl CommunicationListener to add
     */
    public void addCommunicationListener(CommunicationListener<ENTITY_DATA> cl) {
        communicationListeners.add(cl);
    }
    
    /**
     * Removes a CommunicationListener from this Entity.
     * @param cl CommunicationListener to remove
     */
    public void removeCommunicationListener(CommunicationListener<ENTITY_DATA> cl) {
        communicationListeners.remove(cl);
    }
    

    
    /**
     * Get the name of the topic of this entity.
     * @return topic name
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * Get the Participant that has created this Entity.
     * @return Participant
     */
    public Participant getParticipant() {
        return participant;
    }

    /**
     * Gets the type associated with this entity.
     * @return Class
     */
    public Class<T> getType() {
        return type;
    }
    
    /**
     * Gets the typeName of this entity.
     * @return typeName
     */
    public String getTypeName() {
    	return typeName;
    }
    
    /**
     * Gets the Guid of this Entity
     * @return guid
     */
    public Guid getGuid() {
        return guid;
    }
}
