package net.sf.jrtps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jrtps.message.parameter.DataReaderPolicy;
import net.sf.jrtps.message.parameter.DataWriterPolicy;
import net.sf.jrtps.message.parameter.InlineParameter;
import net.sf.jrtps.message.parameter.QosDeadline;
import net.sf.jrtps.message.parameter.QosDestinationOrder;
import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosLatencyBudget;
import net.sf.jrtps.message.parameter.QosLifespan;
import net.sf.jrtps.message.parameter.QosLiveliness;
import net.sf.jrtps.message.parameter.QosOwnership;
import net.sf.jrtps.message.parameter.QosOwnershipStrength;
import net.sf.jrtps.message.parameter.QosPartition;
import net.sf.jrtps.message.parameter.QosPolicy;
import net.sf.jrtps.message.parameter.QosPresentation;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.message.parameter.QosResourceLimits;
import net.sf.jrtps.message.parameter.QosTimeBasedFilter;
import net.sf.jrtps.message.parameter.QosTransportPriority;
import net.sf.jrtps.message.parameter.TopicPolicy;

/**
 * QualityOfService holds a collection QosPolicies.
 * 
 * @author mcr70
 *
 */
public class QualityOfService {
	private static final Logger log = LoggerFactory.getLogger(QualityOfService.class);

	private HashMap<Class<? extends QosPolicy>, QosPolicy> policies = new HashMap<>();

	/**
	 * Constructor with default QosPolicies.
	 */
	public QualityOfService() {
		createDefaultPolicies();
	}

	/**
	 * Sets a given QosPolicy. Old value will be overridden. 
	 *  
	 * @param policy QosPolicy to set.
	 * @throws InconsistentPolicy is thrown if there is some inconsistent value with the policy
	 */
	public void setPolicy(QosPolicy policy) throws InconsistentPolicy {
		checkForInconsistencies(policy);

		policies.put(policy.getClass(), policy);
	}

	private void checkForInconsistencies(QosPolicy policy) throws InconsistentPolicy {
		if (policy instanceof QosDeadline) { // ----------------------  DEADLINE  -----------------
			QosTimeBasedFilter tbf = (QosTimeBasedFilter) policies.get(QosTimeBasedFilter.class);
			if (tbf != null) {
				QosDeadline dl = (QosDeadline) policy;
				if (dl.getPeriod().asMillis() < tbf.getMinimumSeparation().asMillis()) {
					throw new InconsistentPolicy("DEADLINE.period(" + dl.getPeriod() + 
							") must be >= TIME_BASED_FILTER.minimum_separation(" + tbf.getMinimumSeparation() + ")");
				}
			}
		}
		else if (policy instanceof QosTimeBasedFilter) { // ----------  TIME_BASED_FILTER  --------
			QosDeadline dl = (QosDeadline) policies.get(QosDeadline.class);
			if (dl != null) {
				QosTimeBasedFilter tbf = (QosTimeBasedFilter) policy;
				if (dl.getPeriod().asMillis() < tbf.getMinimumSeparation().asMillis()) {
					System.out.println("** " + dl.getPeriod().asMillis() + ", " + tbf.getMinimumSeparation().asMillis());
					throw new InconsistentPolicy("DEADLINE.period(" + dl.getPeriod() + 
							") must be >= TIME_BASED_FILTER.minimum_separation(" + tbf.getMinimumSeparation() + ")");
				}
			}
		}
		else if (policy instanceof QosHistory) { // ------------------  HISTORY  ------------------
			QosResourceLimits rl = (QosResourceLimits) policies.get(QosResourceLimits.class);
			if (rl != null) {
				QosHistory h = (QosHistory) policy;
				if (rl.getMaxSamplesPerInstance() < h.getDepth()) {
					throw new InconsistentPolicy("HISTORY.depth must be <= RESOURCE_LIMITS.max_samples_per_instance");
				}
			}
		}
		else if (policy instanceof QosResourceLimits) { // ----------  RESOURCE_LIMITS  ----------
			QosResourceLimits rl = (QosResourceLimits) policy;
			if (rl.getMaxSamples() < rl.getMaxSamplesPerInstance()) {
				throw new InconsistentPolicy("RESOURCE_LIMITS.max_samples must be >= RESOURCE_LIMITS.max_samples_per_instance");				
			}

			QosHistory h = (QosHistory) policies.get(QosHistory.class);
			if (h != null) {
				if (rl.getMaxSamplesPerInstance() < h.getDepth()) {
					throw new InconsistentPolicy("HISTORY.depth must be <= RESOURCE_LIMITS.max_samples_per_instance");
				}				
			}
		}
	}

	/**
	 * Gets all the DataReaderPolicies from this QualityOfService.
	 * A new Set is always created when calling this method. Changes to returned
	 * Set is not reflected back to this QualityOfService.
	 * 
	 * @return Set<DataReaderPolicy>
	 */
	public Set<DataReaderPolicy> getReaderPolicies() {
		Set<DataReaderPolicy> readerPolicies = new HashSet<>();

		for (QosPolicy qp : policies.values()) {
			if (qp instanceof DataReaderPolicy) {
				readerPolicies.add((DataReaderPolicy) qp);
			}
		}

		return readerPolicies;
	}

	/**
	 * Gets all the DataWriterPolicies from this QualityOfService.
	 * A new Set is always created when calling this method. Changes to returned
	 * Set is not reflected back to this QualityOfService.
	 * 
	 * @return Set<DataWriterPolicy>
	 */
	public Set<DataWriterPolicy> getWriterPolicies() {
		Set<DataWriterPolicy> writerPolicies = new HashSet<>();

		for (QosPolicy qp : policies.values()) {
			if (qp instanceof DataWriterPolicy) {
				writerPolicies.add((DataWriterPolicy) qp);
			}
		}

		return writerPolicies;
	}

	/**
	 * Gets all the TopicPolicies from this QualityOfService.
	 * A new Set is always created when calling this method. Changes to returned
	 * Set is not reflected back to this QualityOfService.
	 * 
	 * @return Set<TopicPolicy>
	 */
	public Set<TopicPolicy> getTopicPolicies() {
		Set<TopicPolicy> topicPolicies = new HashSet<>();

		for (QosPolicy qp : policies.values()) {
			if (qp instanceof TopicPolicy) {
				topicPolicies.add((TopicPolicy) qp);
			}
		}

		return topicPolicies;
	}

	/**
	 * Gets all the inlineable QosPolicies from this QualityOfService.
	 * A new Set is always created when calling this method. Changes to returned
	 * Set is not reflected back to this QualityOfService.
	 * 
	 * @return Set<QosPolicy>
	 */
	public Set<QosPolicy> getInlinePolicies() {
		Set<QosPolicy> inlinePolicies = new HashSet<>();

		for (QosPolicy qp : policies.values()) {
			if (qp instanceof InlineParameter) {
				inlinePolicies.add(qp);
			}
		}

		return inlinePolicies;
	}

	/**
	 * Gets a QosPolicy.
	 * 
	 * @param policyClass Class of the QosPolicy
	 * @return QosPolicy
	 */
	public QosPolicy getPolicy(Class<? extends QosPolicy> policyClass) {
		return policies.get(policyClass);
	}

	
	public boolean isCompatibleWith(QualityOfService other) {
		boolean compatible = true;
		
		for (QosPolicy qp : policies.values()) {
			QosPolicy qpOther = other.getPolicy(qp.getClass());
			if (!qp.isCompatible(qpOther)) {
				compatible = false;
				log.debug("Offered QosPolicy {} is not compatible with requested {}", qp, qpOther);
				// Don't break from the loop. Report every incompatible QoS policy.
			}
		}
		
		return compatible;
	}
	/**
	 * Create default QosPolicies.
	 */
	private void createDefaultPolicies() {
		try {
			setPolicy(QosDeadline.defaultDeadline());
			setPolicy(QosDestinationOrder.defaultDestinationOrder());
			setPolicy(QosDurability.defaultDurability());
			//policies.put(QosDurabilityService.class, QosDurabilityService.defaultDurabilityService());
			setPolicy(QosHistory.defaultHistory());
			setPolicy(QosLatencyBudget.defaultLatencyBudget());
			setPolicy(QosLifespan.defaultLifespan());
			setPolicy(QosLiveliness.defaultLiveliness()); // TODO: check default
			setPolicy(QosOwnership.defaultOwnership()); // TODO: check default
			setPolicy(QosOwnershipStrength.defaultOwnershipStrength()); // TODO: check default
			setPolicy(QosPartition.defaultPartition()); // TODO: check default
			setPolicy(QosPresentation.defaultPresentation()); // TODO: check default
			setPolicy(QosReliability.defaultReliability()); // TODO: check default
			setPolicy(QosResourceLimits.defaultResourceLimits()); // TODO: check default
			setPolicy(QosTransportPriority.defaultTransportPriority()); // TODO: check default
			setPolicy(QosTimeBasedFilter.defaultTimeBasedFilter()); // TODO: check default
		} catch (InconsistentPolicy e) {
			log.error("Internal error", e);
			throw new RuntimeException(e);
		} 

		// QosUserData, QosGroupData, QosTopicData is omitted. 		
	}
	
	
}
