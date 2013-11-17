package net.sf.jrtps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import net.sf.jrtps.message.parameter.DataReaderPolicy;
import net.sf.jrtps.message.parameter.DataWriterPolicy;
import net.sf.jrtps.message.parameter.InlineParameter;
import net.sf.jrtps.message.parameter.QosDeadline;
import net.sf.jrtps.message.parameter.QosDestinationOrder;
import net.sf.jrtps.message.parameter.QosDurability;
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
	private HashMap<Class<? extends QosPolicy>, QosPolicy> policies = new HashMap<>();
	
	/**
	 * Constructor with default QosPolicies.
	 */
	public QualityOfService() {
		createDefaultPolicies();
	}
	
	/**
	 * Sets a given QosPolicy.
	 * @param policy
	 */
	public void setPolicy(QosPolicy policy) {
		policies.put(policy.getClass(), policy);
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

	/**
	 * Create default QosPolicies.
	 */
	private void createDefaultPolicies() {
		policies.put(QosDeadline.class, QosDeadline.defaultDeadline());
		policies.put(QosDestinationOrder.class, QosDestinationOrder.defaultDestinationOrder());
		policies.put(QosDurability.class, QosDurability.defaultDurability());
		//policies.put(QosDurabilityService.class, QosDurabilityService.defaultDurabilityService());
		//policies.put(QosHistory.class, QosHistory.defaultHistory());
		policies.put(QosLatencyBudget.class, QosLatencyBudget.defaultLatencyBudget());
		policies.put(QosLifespan.class, QosLifespan.defaultLifespan());
		policies.put(QosLiveliness.class, QosLiveliness.defaultLiveliness()); // TODO: check default
		policies.put(QosOwnership.class, QosOwnership.defaultOwnership()); // TODO: check default
		policies.put(QosOwnershipStrength.class, QosOwnershipStrength.defaultOwnershipStrength()); // TODO: check default
		policies.put(QosPartition.class, QosPartition.defaultPartition()); // TODO: check default
		policies.put(QosPresentation.class, QosPresentation.defaultPresentation()); // TODO: check default
		policies.put(QosReliability.class, QosReliability.defaultReliability()); // TODO: check default
		policies.put(QosResourceLimits.class, QosResourceLimits.defaultResourceLimits()); // TODO: check default
		policies.put(QosTransportPriority.class, QosTransportPriority.defaultTransportPriority()); // TODO: check default
		policies.put(QosTimeBasedFilter.class, QosTimeBasedFilter.defaultTimeBasedFilter()); // TODO: check default
		
		// QosUserData, QosGroupData, QosTopicData is omitted. 		
	}
}
