package net.sf.jrtps.message.parameter;

/**
 * A tagging interface used to denote a Parameter as a Quality of Service param.
 * 
 * @author mcr70
 *
 */
public interface QosPolicy {
	/**
	 * Checks, if this QosPolicy is compatible with other QosPolicy
	 * @param other
	 * @return true, if QosPolicy is compatible
	 */
	public boolean isCompatible(QosPolicy other);
}
