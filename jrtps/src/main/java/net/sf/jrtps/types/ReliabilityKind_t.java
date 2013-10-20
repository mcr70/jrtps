package net.sf.jrtps.types;

/**
 * 
 * @author mcr70
 * @see 9.3.2 Mapping of the Types that Appear Within Submessages or Built-in Topic Data
 */
public class ReliabilityKind_t {
	public static final ReliabilityKind_t BEST_EFFORT = new ReliabilityKind_t(1);
	public static final ReliabilityKind_t RELIABLE = new ReliabilityKind_t(3);
	
	private int value;
	
	private ReliabilityKind_t(int value) {
		this.value = value;
	}
}
