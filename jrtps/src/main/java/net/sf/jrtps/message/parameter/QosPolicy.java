package net.sf.jrtps.message.parameter;

/**
 * An interface used to denote a Parameter as a Quality of Service policy parameter.
 * 
 * @author mcr70
 *
 */
public interface QosPolicy {
	/**
	 * Checks, if this QosPolicy is compatible with other QosPolicy
	 * 
	 * @param other
	 * @return true, if QosPolicy is compatible
	 */
	public boolean isCompatible(QosPolicy other);
}
