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
    private static final Logger log = LoggerFactory.getLogger(QualityOfService.class);
    private static final QualityOfService sedpQos = createSEDPQualityOfService();
    private static final QualityOfService spdpQos = createSPDPQualityOfService();
    private HashMap<Class<? extends QosPolicy>, QosPolicy> policies = new HashMap<>();

    /**
     * Constructor with default QosPolicies.
     */
    public QualityOfService() {
        //createDefaultPolicies();
    }

    /**
     * Sets a given QosPolicy. Old value will be overridden.
     * 
     * @param policy
     *            QosPolicy to set.
     * @throws InconsistentPolicy
     *             is thrown if there is some inconsistent value with the policy
     */
    public void setPolicy(QosPolicy policy) throws InconsistentPolicy {
        checkForInconsistencies(policy);

        policies.put(policy.getClass(), policy);
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

            if (rl.getMaxSamplesPerInstance() != -1 && rl.getMaxSamplesPerInstance() < h.getDepth()) {
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
            if (qp instanceof InlineParameter) {
                inlinePolicies.add(qp);
            }
        }

        return inlinePolicies;
    }

    
    @SuppressWarnings("unchecked")
    public boolean isCompatibleWith(QualityOfService other) {
        boolean compatible = true;

        for (QosPolicy qp : policies.values()) {
            QosPolicy qpOther = other.policies.get(qp.getClass());
            if (!qp.isCompatible(qpOther)) {
                compatible = false;
                log.warn("Offered QosPolicy {} is not compatible with requested {}", qp, qpOther);
                // Don't break from the loop. Report every incompatible QoS
                // policy.
            }
        }

        return compatible;
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


    
    private static QualityOfService createSEDPQualityOfService() {
        QualityOfService qos = new QualityOfService();
        try {
            qos.setPolicy(new QosDurability(QosDurability.Kind.TRANSIENT)); 
            // TODO: OSPL uses TRANSIENT, while TRANSIENT_LOCAL would be correct
            
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
}
