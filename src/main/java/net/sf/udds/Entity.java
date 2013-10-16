package net.sf.udds;

public class Entity {
	private String topicName;

	protected Entity(String topicName) {
		this.topicName = topicName;
	}
	
	public String getTopicName() {
		return topicName;
	}
}
