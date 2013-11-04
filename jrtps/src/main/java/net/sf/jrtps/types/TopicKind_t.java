package net.sf.jrtps.types;

/**
 * see 9.3.2 Mapping of the Types that Appear Within Submessages or Built-in Topic Data
 * 
 * @author mcr70
 * 
 */
public class TopicKind_t {
	public static final TopicKind_t NO_KEY = new TopicKind_t(1);
	public static final TopicKind_t WITH_KEY = new TopicKind_t(2); 
	
	int value; // TODO: is java long the same as DDS long
	
	private TopicKind_t(int value) {
		this.value = value;
	}
}
