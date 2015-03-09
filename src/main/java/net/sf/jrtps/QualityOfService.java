package net.sf.jrtps;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import net.sf.jrtps.message.parameter.Changeable;
import net.sf.jrtps.message.parameter.DataReaderPolicy;
import net.sf.jrtps.message.parameter.DataWriterPolicy;
import net.sf.jrtps.message.parameter.InlineQoS;
import net.sf.jrtps.message.parameter.Parameter;
import net.sf.jrtps.message.parameter.QosDataRepresentation;
import net.sf.jrtps.message.parameter.QosDeadline;
import net.sf.jrtps.message.parameter.QosDestinationOrder;
import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.QosDurabilityService;
import net.sf.jrtps.message.parameter.QosGroupData;
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
import net.sf.jrtps.message.parameter.QosTopicData;
import net.sf.jrtps.message.parameter.QosTransportPriority;
import net.sf.jrtps.message.parameter.QosTypeConsistencyEnforcement;
import net.sf.jrtps.message.parameter.QosUserData;
import net.sf.jrtps.message.parameter.TopicPolicy;
import net.sf.jrtps.types.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QualityOfService holds a collection QosPolicies.
 * 
 * @author mcr70
 * 
 */
@SuppressWarnings("rawtypes")
public class QualityOfService {
	private static final Logger logger = LoggerFactory.getLogger(QualityOfService.class);

    private static final QualityOfService sedpQos = createSEDPQualityOfService();
    private static final QualityOfService spdpQos = createSPDPQualityOfService();
    
    private final List<PolicyListener> policyListeners = new CopyOnWriteArrayList<QualityOfService.PolicyListener>(); 
    private final HashMap<Class<? extends QosPolicy>, QosPolicy> policies = new HashMap<>();

    /**
     * Constructor with default QosPolicies.
     */
    public QualityOfService() {
    }

    /**
     * Sets a given QosPolicy. Old value will be overridden.
     * 
     * @param policy QosPolicy to set.
     * @throws InconsistentPolicy is thrown if there is some inconsistent value with the policy
     */
    public void setPolicy(QosPolicy policy) throws InconsistentPolicy {
    	if (!(policy instanceof Changeable)) {
    		synchronized (policies) {
    			if (policies.containsKey(policy.getClass())) {
    				logger.debug("{} was already within this QualityOfService: {}", policy.getClass(),
    						policies.keySet());
    				throw new IllegalArgumentException(policy.getClass().getSimpleName() + 
    						" is not Changeable; cannot set twice");
    			}
    		}
		}
    	
    	checkForInconsistencies(policy);

        policies.put(policy.getClass(), policy);
        notifyPolicyChanged(policy);
    }

    private void notifyPolicyChanged(QosPolicy policy) {
    	for (PolicyListener listener : policyListeners) {
    		listener.policyChanged(policy);
    	}
    }

	private void checkForInconsistencies(QosPolicy policy) throws InconsistentPolicy {
        if (policy instanceof QosDeadline) { // ---  DEADLINE  ---
            QosTimeBasedFilter tbf = (QosTimeBasedFilter) policies.get(QosTimeBasedFilter.class);
            if (tbf == null) {
                tbf = QosTimeBasedFilter.defaultTimeBasedFilter();
            }

            QosDeadline dl = (QosDeadline) policy;
            if (dl.getPeriod().asMillis() < tbf.getMinimumSeparation().asMillis()) {
                throw new InconsistentPolicy("DEADLINE.period(" + dl.getPeriod()
                        + ") must be >= TIME_BASED_FILTER.minimum_separation(" + tbf.getMinimumSeparation() + ")");
            }
        } 
        else if (policy instanceof QosTimeBasedFilter) { // ---  TIME_BASED_FILTER  ---
            QosDeadline dl = (QosDeadline) policies.get(QosDeadline.class);
            if (dl == null) {
                dl = QosDeadline.defaultDeadline();
            }

            QosTimeBasedFilter tbf = (QosTimeBasedFilter) policy;
            if (dl.getPeriod().asMillis() < tbf.getMinimumSeparation().asMillis()) {
                throw new InconsistentPolicy("DEADLINE.period(" + dl.getPeriod()
                        + ") must be >= TIME_BASED_FILTER.minimum_separation(" + tbf.getMinimumSeparation() + ")");
            }
        } 
        else if (policy instanceof QosHistory) { // ---  HISTORY  ---
            QosResourceLimits rl = (QosResourceLimits) policies.get(QosResourceLimits.class);
            if (rl == null) {
                rl = QosResourceLimits.defaultResourceLimits();
            }

            QosHistory h = (QosHistory) policy;

            if (rl.getMaxSamplesPerInstance() != -1 && 
                    QosHistory.Kind.KEEP_LAST == h.getKind() &&
                    rl.getMaxSamplesPerInstance() < h.getDepth()) {
                throw new InconsistentPolicy("HISTORY.depth must be <= RESOURCE_LIMITS.max_samples_per_instance");
            }
        } 
        else if (policy instanceof QosResourceLimits) { // ---  RESOURCE_LIMITS  ---
            QosResourceLimits rl = (QosResourceLimits) policy;
            if (rl.getMaxSamples() < rl.getMaxSamplesPerInstance()) {
                throw new InconsistentPolicy(
                        "RESOURCE_LIMITS.max_samples must be >= RESOURCE_LIMITS.max_samples_per_instance");
            }

            QosHistory h = (QosHistory) policies.get(QosHistory.class);
            if (h == null) {
                h = QosHistory.defaultHistory();
            }

            if (rl.getMaxSamplesPerInstance() != -1 && rl.getMaxSamplesPerInstance() < h.getDepth()) {
                throw new InconsistentPolicy("HISTORY.depth must be <= RESOURCE_LIMITS.max_samples_per_instance");
            }
        }
    }

    /**
     * Gets all the DataReaderPolicies from this QualityOfService. A new Set is
     * always created when calling this method. Changes to returned Set is not
     * reflected back to this QualityOfService.
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
     * Gets all the DataWriterPolicies from this QualityOfService. A new Set is
     * always created when calling this method. Changes to returned Set is not
     * reflected back to this QualityOfService.
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
     * Gets all the TopicPolicies from this QualityOfService. A new Set is
     * always created when calling this method. Changes to returned Set is not
     * reflected back to this QualityOfService.
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
     * Gets all the inlineable QosPolicies from this QualityOfService. A new Set
     * is always created when calling this method. Changes to returned Set is
     * not reflected back to this QualityOfService.
     * 
     * @return Set<QosPolicy>
     */
    public Set<QosPolicy<?>> getInlinePolicies() {
        Set<QosPolicy<?>> inlinePolicies = new HashSet<>();

        for (QosPolicy qp : policies.values()) {
            if (qp instanceof InlineQoS) {
                inlinePolicies.add(qp);
            }
        }

        return inlinePolicies;
    }

    /**
     * Gets all the incompatible policies that are found from either QualityOfService.
     * I.e. check only policies that are explicitly set on either QualityOfService. 

     * @param other Other QoS 
     * @return a Set of QosPolicies. If all the policies are compatible, return an empty Set.
     */
    @SuppressWarnings("unchecked")
    public Set<QosPolicy<?>> getIncompatibleQos(QualityOfService other) {
        // Join qos policies defined in both QoS
        Set<Class<? extends QosPolicy>> commonSet = new HashSet<>();
        commonSet.addAll(policies.keySet());
        commonSet.addAll(other.policies.keySet());
        
        Set<QosPolicy<?>> set = new HashSet<>();

        // Loop through each qos policy defined in either of the QoS
        for (Class<? extends QosPolicy> clazz : commonSet) {
            if (!Parameter.class.isAssignableFrom(clazz)) {
                continue;
            }
            
            QosPolicy qp = policies.get(clazz);
            if (qp == null) {
                qp = getDefaultFor(clazz); // get default for 'this'
            }
            
            QosPolicy qpOther = other.policies.get(clazz);
            if (qpOther == null) {
                qpOther = other.getDefaultFor(clazz); // get default for 'other'
            }
            
            if (!qp.isCompatible(qpOther)) {
                set.add(qpOther);
                logger.warn("Offered QosPolicy {} is not compatible with requested {}", qp, qpOther);
            }
        }

        return set;        
    }
    
    private QosPolicy getDefaultFor(Class<? extends QosPolicy> clazz) {
        if (QosDeadline.class.equals(clazz)) {
            return QosDeadline.defaultDeadline();
        }
        else if (QosDestinationOrder.class.equals(clazz)) {
            return QosDestinationOrder.defaultDestinationOrder();
        }
        else if (QosDurability.class.equals(clazz)) {
            return QosDurability.defaultDurability();
        }
        else if (QosDurabilityService.class.equals(clazz)) {
            return QosDurabilityService.defaultDurabilityService();
        }
        else if (QosGroupData.class.equals(clazz)) {
            return QosGroupData.defaultGroupData();
        }
        else if (QosHistory.class.equals(clazz)) {
            return QosHistory.defaultHistory();
        }
        else if (QosLatencyBudget.class.equals(clazz)) {
            return QosLatencyBudget.defaultLatencyBudget();
        }
        else if (QosLifespan.class.equals(clazz)) {
            return QosLifespan.defaultLifespan();
        }
        else if (QosLiveliness.class.equals(clazz)) {
            return QosLiveliness.defaultLiveliness();
        }
        else if (QosOwnership.class.equals(clazz)) {
            return QosOwnership.defaultOwnership();
        }
        else if (QosOwnershipStrength.class.equals(clazz)) {
            return QosOwnershipStrength.defaultOwnershipStrength();
        }
        else if (QosPartition.class.equals(clazz)) {
            return QosPartition.defaultPartition();
        }
        else if (QosPresentation.class.equals(clazz)) {
            return QosPresentation.defaultPresentation();
        }
        else if (QosOwnershipStrength.class.equals(clazz)) {
            return QosOwnershipStrength.defaultOwnershipStrength();
        }
        else if (QosReliability.class.equals(clazz)) {
            return QosReliability.defaultReliability();
        }
        else if (QosResourceLimits.class.equals(clazz)) {
            return QosResourceLimits.defaultResourceLimits();
        }
        else if (QosTopicData.class.equals(clazz)) {
            return QosTopicData.defaultTopicData();
        }
        else if (QosTransportPriority.class.equals(clazz)) {
            return QosTransportPriority.defaultTransportPriority();
        }
        else if (QosTimeBasedFilter.class.equals(clazz)) {
            return QosTimeBasedFilter.defaultTimeBasedFilter();
        }
        else if (QosUserData.class.equals(clazz)) {
            return QosUserData.defaultUserData();
        }
        else if (QosTypeConsistencyEnforcement.class.equals(clazz)) {
            return QosTypeConsistencyEnforcement.defaultTypeConsistencyEnforcement();
        }

        logger.warn("Don't know how to get default for {}", clazz);

        return null;
    }

    /**
     * Checks wheter or not this QualityOfService is compatible with the other.
     * @param other Other QualityOfService
     * @return true, if compatible
     */
    public boolean isCompatibleWith(QualityOfService other) {
        return getIncompatibleQos(other).size() == 0;
    }

    /**
     * Gets the QualityOfService used in Simple Endpoint Discovery
     * Protocol(SEDP)
     * 
     * @return QualityOfService for SEDP
     */
    public static QualityOfService getSEDPQualityOfService() {
        return sedpQos;
    }

    /**
     * Gets the QualityOfService used in Simple Participant Discovery
     * Protocol(SPDP)
     * 
     * @return QualityOfService for SPDP
     */
    public static QualityOfService getSPDPQualityOfService() {
        return spdpQos;
    }


    /**
     * Gets the DataRepresentation QosPolicy
     * @return QosDataRepresentation
     */
    public QosDataRepresentation getDataRepresentation() {
        QosPolicy policy = policies.get(QosDataRepresentation.class);
        if (policy != null) {
            return (QosDataRepresentation) policy;
        }
        
        return QosDataRepresentation.defaultDataRepresentation();
    }
    
    /**
     * Gets the Deadline QosPolicy
     * @return QosDeadline
     */
    public QosDeadline getDeadline() {
        QosPolicy policy = policies.get(QosDeadline.class);
        if (policy != null) {
            return (QosDeadline) policy;
        }
        
        return QosDeadline.defaultDeadline();
    }

    /**
     * Gets the Deadline QosPolicy
     * @return QosDeadline
     */
    public QosDestinationOrder getDestinationOrder() {
        QosPolicy policy = policies.get(QosDestinationOrder.class);
        if (policy != null) {
            return (QosDestinationOrder) policy;
        }
        
        return QosDestinationOrder.defaultDestinationOrder();
    }

    /**
     * Gets the Durability QosPolicy
     * @return QosDurability
     */
    public QosDurability getDurability() {
        QosPolicy policy = policies.get(QosDurability.class);
        if (policy != null) {
            return (QosDurability) policy;
        }
        
        return QosDurability.defaultDurability(); 
    }

    /**
     * Gets the DurabilityService QosPolicy
     * @return QosDurability
     */
    public QosDurabilityService getDurabilityService() {
        QosPolicy policy = policies.get(QosDurabilityService.class);
        if (policy != null) {
            return (QosDurabilityService) policy;
        }
        
        return QosDurabilityService.defaultDurabilityService(); 
    }

    /**
     * Gets the GroupData QosPolicy
     * @return QosGroupData
     */
    public QosGroupData getGroupData() { 
        QosPolicy policy = policies.get(QosGroupData.class);
        if (policy != null) {
            return (QosGroupData) policy;
        }
        
        return QosGroupData.defaultGroupData(); 
    }

    /**
     * Gets the History QosPolicy
     * @return QosHistory
     */
    public QosHistory getHistory() { 
        QosPolicy policy = policies.get(QosHistory.class);
        if (policy != null) {
            return (QosHistory) policy;
        }
        
        return QosHistory.defaultHistory(); 
    }

    /**
     * Gets the LatencyBudget QosPolicy
     * @return QosLatencyBudget
     */
    public QosLatencyBudget getLatencyBudget() { 
        QosPolicy policy = policies.get(QosLatencyBudget.class);
        if (policy != null) {
            return (QosLatencyBudget) policy;
        }
        
        return QosLatencyBudget.defaultLatencyBudget();
    }

    /**
     * Gets the Lifespan QosPolicy
     * @return QosLifespan
     */
    public QosLifespan getLifespan() {
        QosPolicy policy = policies.get(QosLifespan.class);
        if (policy != null) {
            return (QosLifespan) policy;
        }
        
        return QosLifespan.defaultLifespan();
    }

    /**
     * Gets the Liveliness QosPolicy
     * @return QosLiveliness
     */
    public QosLiveliness getLiveliness() {
        QosPolicy policy = policies.get(QosLiveliness.class);
        if (policy != null) {
            return (QosLiveliness) policy;
        }
        
        return QosLiveliness.defaultLiveliness();
    }

    /**
     * Gets the Ownership QosPolicy
     * @return QosOwnership
     */
    public QosOwnership getOwnership() { 
        QosPolicy policy = policies.get(QosOwnership.class);
        if (policy != null) {
            return (QosOwnership) policy;
        }
        
        return QosOwnership.defaultOwnership();  
    }
    
    /**
     * Gets the OwnershipStrength QosPolicy
     * @return QosOwnershipStrength
     */
    public QosOwnershipStrength getOwnershipStrength() { 
        QosPolicy policy = policies.get(QosOwnershipStrength.class);
        if (policy != null) {
            return (QosOwnershipStrength) policy;
        }
        
        return QosOwnershipStrength.defaultOwnershipStrength();  
    }
    
    /**
     * Gets the Partition QosPolicy
     * @return QosPartition
     */
    public QosPartition getPartition() { 
        QosPolicy policy = policies.get(QosPartition.class);
        if (policy != null) {
            return (QosPartition) policy;
        }
        
        return QosPartition.defaultPartition();  
    }

    /**
     * Gets the Presentation QosPolicy
     * @return QosPresentation
     */
    public QosPresentation getPresentation() { 
        QosPolicy policy = policies.get(QosPresentation.class);
        if (policy != null) {
            return (QosPresentation) policy;
        }
        
        return QosPresentation.defaultPresentation();  
    }

    /**
     * Gets the Reliability QosPolicy
     * @return QosReliability
     */
    public QosReliability getReliability() { 
        QosPolicy policy = policies.get(QosReliability.class);
        if (policy != null) {
            return (QosReliability) policy;
        }
        
        return QosReliability.defaultReliability(); 
    }

    /**
     * Gets the ResourceLimits QosPolicy
     * @return QosResourceLimits
     */
    public QosResourceLimits getResourceLimits() { 
        QosPolicy policy = policies.get(QosResourceLimits.class);
        if (policy != null) {
            return (QosResourceLimits) policy;
        }
        
        return QosResourceLimits.defaultResourceLimits(); 
    }

    /**
     * Gets the TopicData QosPolicy
     * @return QosTopicData
     */
    public QosTopicData getTopicData() { 
        QosPolicy policy = policies.get(QosTopicData.class);
        if (policy != null) {
            return (QosTopicData) policy;
        }
        
        return QosTopicData.defaultTopicData(); 
    }

    /**
     * Gets the TransportPriority QosPolicy
     * @return QosTransportPriority
     */
    public QosTransportPriority getTransportPriority() { 
        QosPolicy policy = policies.get(QosTransportPriority.class);
        if (policy != null) {
            return (QosTransportPriority) policy;
        }
        
        return QosTransportPriority.defaultTransportPriority();
    }

    /**
     * Gets the TimeBasedFilter QosPolicy
     * @return QosTimeBasedFilter
     */
    public QosTimeBasedFilter getTimeBasedFilter() { 
        QosPolicy policy = policies.get(QosTimeBasedFilter.class);
        if (policy != null) {
            return (QosTimeBasedFilter) policy;
        }
        
        return QosTimeBasedFilter.defaultTimeBasedFilter(); 
    }

    /**
     * Gets the TransportPriority QosPolicy
     * @return QosTransportPriority
     */
    public QosUserData getUserData() { 
        QosPolicy policy = policies.get(QosUserData.class);
        if (policy != null) {
            return (QosUserData) policy;
        }
        
        return QosUserData.defaultUserData();
    }

    
    public QosTypeConsistencyEnforcement getTypeConsistencyEnforcement() {
        QosPolicy policy = policies.get(QosTypeConsistencyEnforcement.class);
        if (policy != null) {
            return (QosTypeConsistencyEnforcement) policy;
        }
        
        return QosTypeConsistencyEnforcement.defaultTypeConsistencyEnforcement();
    }

    
    private static QualityOfService createSEDPQualityOfService() {
        QualityOfService qos = new QualityOfService();
        try {
            qos.setPolicy(new QosDurability(QosDurability.Kind.TRANSIENT)); 
            // TODO: OSPL uses TRANSIENT, while TRANSIENT_LOCAL would be correct
            
            //qos.setPolicy(new QosPartition(new String[]{".*"})); // TODO: OSPL has "__BUILT-IN PARTITION__"
            
            qos.setPolicy(new QosPresentation(QosPresentation.Kind.TOPIC, false, false));
            qos.setPolicy(new QosDeadline(Duration.INFINITE));
            qos.setPolicy(new QosOwnership(QosOwnership.Kind.SHARED));
            qos.setPolicy(new QosLiveliness(QosLiveliness.Kind.AUTOMATIC, new Duration(0, 0)));
            qos.setPolicy(new QosTimeBasedFilter(new Duration(0)));
            qos.setPolicy(new QosReliability(QosReliability.Kind.RELIABLE, new Duration(100)));
            qos.setPolicy(new QosDestinationOrder(QosDestinationOrder.Kind.BY_RECEPTION_TIMESTAMP));
            qos.setPolicy(new QosHistory(QosHistory.Kind.KEEP_LAST, 1));
            qos.setPolicy(new QosResourceLimits(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE));
        } catch (InconsistentPolicy e) {
            throw new RuntimeException("Internal error", e);
        }

        return qos;
    }

    private static QualityOfService createSPDPQualityOfService() {
        QualityOfService qos = new QualityOfService();
        try {
            qos.setPolicy(new QosDurability(QosDurability.Kind.TRANSIENT_LOCAL));
            qos.setPolicy(new QosReliability(QosReliability.Kind.BEST_EFFORT, new Duration(0)));
        } catch (InconsistentPolicy e) {
            throw new RuntimeException("Internal error", e);
        }

        return qos;
    }

    public String toString() {
        return policies.values().toString();
    }

    
    public void addPolicyListener(PolicyListener listener) {
    	policyListeners.add(listener);
    }
    
    /**
     * PolicyChangedListener can be used to track changes to QualityOfService.
     * 
     * @author mcr70
     */
    public interface PolicyListener {
    	void policyChanged(QosPolicy policy);
    }
}
