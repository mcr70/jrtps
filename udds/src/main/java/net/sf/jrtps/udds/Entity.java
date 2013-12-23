package net.sf.jrtps.udds;

/**
 * Entity is a base class for DataReader and DataWriter.
 * @author mcr70
 *
 */
public class Entity {
	private final String topicName;
	private final Participant participant;
	
	/**
	 * Constructor
	 * @param topicName name of the topic this entity is bound to.
	 */
	protected Entity(Participant p, String topicName) {
		this.participant = p;
		this.topicName = topicName;
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
	 * 
	 * @return Participant
	 */
	public Participant getParticipant() {
		return participant;
	}
}
