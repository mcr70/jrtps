package net.sf.jrtps.message.parameter;

/**
 * An interface used to denote a Parameter as a Quality of Service policy parameter.
 * 
 * @author mcr70
 *
 */
public interface QosPolicy<T> {
	/**
	 * Checks, if this QosPolicy is compatible with other QosPolicy.
	 * 'this' QosPolicy should be considered as 'offered' policy, and
	 * 'other' should be considered as 'requested' policy.
	 * 
	 * @param requested Requested QosPolicy
	 * @return true, if QosPolicy is compatible
	 */
	public boolean isCompatible(T requested);
}
