package net.sf.jrtps.builtin;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.InlineQoS;
import net.sf.jrtps.message.parameter.Parameter;
import net.sf.jrtps.message.parameter.ParameterId;
import net.sf.jrtps.message.parameter.QosPolicy;
import net.sf.jrtps.types.Guid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for data, that is received during discovery.
 * 
 * @author mcr70
 */
public class DiscoveredData {
    private static final Logger logger = LoggerFactory.getLogger(DiscoveredData.class);

	// While reading data from stream, qos policies might come in 'wrong' order.
    // This list keeps track of inconsistencies occured
    private List<QosPolicy<?>> inconsistentPolicies = new LinkedList<>();
    private List<Parameter> params = new LinkedList<>();

    protected QualityOfService qos;

    protected String typeName;
    protected String topicName;
    // On spec, key is BuiltinTopicKey_t (4 bytes), but KeyHash Parameter is 16
    // bytes.
    // see table 9.10, 9.6.3.3 KeyHash.
    // interpretation is, for builtin topics, key is
    // guid_prefix(12) + builtin_topic_key(4), which is equal to prefix(12) +
    // entityid(4), which is guid
    protected Guid key;

    /**
     * This constructor is used when DiscoveredData is being created from
     * RTPSByteBuffer
     */
    protected DiscoveredData() {
        qos = new QualityOfService(); // Initialize QoS with default policies.
    }

    /**
     * This constructor is used when DiscoveredData is being created from
     * scratch
     * 
     * @param typeName Type name of the data
     * @param topicName name of the topic
     * @param key guid of the remote entity, which acts as a key for topic
     * @param qos QualityOfService of discovered entity
     */
    protected DiscoveredData(String typeName, String topicName, Guid key, QualityOfService qos) {
        this.typeName = typeName;
        this.topicName = topicName;
        this.key = key;
        this.qos = qos;
    }

    /**
     * This method must be called after all the Parameters have been read from
     * the stream to resolve possible inconsistent policies.
     * 
     * @throws InconsistentPolicy
     *             if discovered data would contain inconsistent policies.
     */
    protected void resolveInconsistencies() throws InconsistentPolicy {
        if (inconsistentPolicies.size() > 0) {
        	logger.debug("resolveInconsistencies: {}", inconsistentPolicies);
            resolveInconsistencies(inconsistentPolicies);
        }
    }

    /**
     * Adds a Parameter that was not handled by subclass.
     * 
     * @param param Parameter
     */
    protected void addParameter(Parameter param) {
        params.add(param);
    }

    /**
     * Gets all the parameters that were received during discovery.
     * 
     * @return parameters
     */
    public List<Parameter> getParameters() {
        return params;
    }

    private void resolveInconsistencies(List<QosPolicy<?>> inconsistencies) throws InconsistentPolicy {
        int size = inconsistencies.size();

        for (QosPolicy<?> qp : inconsistencies) {
            try {
                qos.setPolicy(qp);
                inconsistencies.remove(qp);
            } catch (InconsistentPolicy e) {
                // Ignore during resolve
            }
        }

        int __size = inconsistencies.size();

        if (size != __size) { // If the size changes, recursively call again
            resolveInconsistencies(inconsistencies);
        } 
        else {
            if (inconsistencies.size() > 0) {
                throw new InconsistentPolicy(inconsistencies.toString());
            }
        }
    }

    /**
     * Get the type name that is associated with this DiscoveresData. Type name
     * is formed from the PID_TYPE_NAME parameter
     * 
     * @return typeName
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Get the topic name that is associated with this DiscoveredData. Topic
     * name is formed from the PID_TOPIC_NAME parameter
     * 
     * @return topicName
     */
    public String getTopicName() {
        return topicName;
    }

    /**
     * Gets the Guid of the entity represented by this DiscoveredData. 
     * 
     * @return key as Guid
     */
    public Guid getBuiltinTopicKey() {
        return key;
    }

    /**
     * Gets the QualityOfService of this DiscoveredData.
     * 
     * @return QualityOfService
     */
    public QualityOfService getQualityOfService() {
        return qos;
    }

    /**
     * Gets the Parameter with given ParameterEnum
     * @param id Id of the Parameter
     * @return Parameter, or null if not found
     */
    public Parameter getParameter(ParameterId id) {
        for (Parameter p : params) {
            if (p.getParameterId() == id) {
                return p;
            }
        }
        
        return null;
    }
    
    /**
     * Adds a QosPolicy.
     * 
     * @param policy QosPolicy to add
     */
    protected void addQosPolicy(QosPolicy<?> policy) {
        try {
            qos.setPolicy(policy);
        } catch (InconsistentPolicy e) {
            inconsistentPolicies.add(policy);
        }
    }

    /**
     * Get inlineable Qos policies. Such policies implement InlineParameter.
     * 
     * @return QosPolicies that can be inlined to Data
     * @see net.sf.jrtps.message.Data#getInlineQos()
     * @see InlineQoS
     */
    public Set<QosPolicy<?>> getInlineableQosPolicies() {
        return qos.getInlinePolicies();
    }

    public String toString() {
        return topicName + "(" + typeName + "): " + key + ", QoS: " + qos;
    }
}
