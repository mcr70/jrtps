package net.sf.jrtps.udds;

/**
 * Entity is a base class for DataReader and DataWriter.
 * 
 * @author mcr70
 * @param <T>
 * 
 */
public class Entity<T> {
    private final String topicName;
    private final Participant participant;
    private final Class<T> type;

    /**
     * Constructor
     * 
     * @param topicName
     *            name of the topic this entity is bound to.
     */
    protected Entity(Participant p, Class<T> type, String topicName) {
        this.participant = p;
        this.type = type;
        this.topicName = topicName;
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
}
