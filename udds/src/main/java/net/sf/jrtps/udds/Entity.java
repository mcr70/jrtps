package net.sf.jrtps.udds;

/**
 * Entity is a base class for DataReader and DataWriter.
 * @author mcr70
 *
 */
public class Entity {
	private String topicName;
	
	/**
	 * Constructor
	 * @param topicName name of the topic this entity is bound to.
	 */
	protected Entity(String topicName) {
		this.topicName = topicName;
	}
	
	/**
	 * Get the name of the topic of this entity.
	 * @return topic name
	 */
	public String getTopicName() {
		return topicName;
	}
}
