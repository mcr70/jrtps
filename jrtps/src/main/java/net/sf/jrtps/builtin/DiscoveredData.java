package net.sf.jrtps.builtin;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.InlineParameter;
import net.sf.jrtps.message.parameter.QosPolicy;
import net.sf.jrtps.types.GUID_t;

public class DiscoveredData {
	// While reading data from stream, qos policies might come in 'wrong' order.
	// This list keeps track of inconsistencies occured
	private List<QosPolicy> inconsistenPolicies = new LinkedList<>(); 
	private final QualityOfService qos;
	
	protected String typeName;
	protected String topicName;
	// On spec, key is BuiltinTopicKey_t (4 bytes), but KeyHash Parameter is 16 bytes.
	// see table 9.10, 9.6.3.3 KeyHash.
	// interpretation is, for builtin topics, key is 
	//   guid_prefix(12) + builtin_topic_key(4), which is equal to prefix(12) + entityid(4), which is guid
	protected GUID_t key;  

	
	/**
	 * This constructor is used when DiscoveredData is being created from 
	 * RTPSByteBuffer
	 */
	protected DiscoveredData() {
		qos = new QualityOfService(); // Initialize QoS with default policies.
	}
	
	/**
	 * This constructor is used when DiscoveredData is being created from scratch
	 * 
	 * @param typeName
	 * @param topicName
	 * @param key
	 * @param qos2 
	 */
	protected DiscoveredData(String typeName, String topicName, GUID_t key, QualityOfService qos) {
		this.typeName = typeName;
		this.topicName = topicName;
		this.key = key;
		this.qos = qos;
	}
	
	/**
	 * This method must be called after all the Parameters have been read from the stream to
	 * resolve possible inconsistent policies. 
	 * 
	 * @throws InconsistentPolicy if discovered data would contain inconsistent policies. 
	 */
	protected void resolveInconsistencies() throws InconsistentPolicy {
		if (inconsistenPolicies.size() > 0) {
			resolveInconsistencies(inconsistenPolicies);
		}
	}
	
	private void resolveInconsistencies(List<QosPolicy> inconsistentPolicies) throws InconsistentPolicy {
		int size = inconsistenPolicies.size();
		
		for (QosPolicy qp : inconsistentPolicies) {
			try {
				qos.setPolicy(qp);
				inconsistenPolicies.remove(qp);
			} catch (InconsistentPolicy e) {
				// Ignore during resolve
			}
		}
		
		int __size = inconsistenPolicies.size();
		
		if (size != __size) {
			resolveInconsistencies(inconsistentPolicies);
		}
		else {
			if (inconsistentPolicies.size() > 0) {
				throw new InconsistentPolicy(inconsistenPolicies.toString());
			}
		}
	}
	
	/**
	 * Get the type name that is associated with this DiscoveresData.
	 * Type name is formed from the PID_TYPE_NAME parameter
	 * 
	 * @return typeName
	 */
	public String getTypeName() {
		return typeName;
	}
	
	/**
	 * Get the topic name that is associated with this DiscoveredData.
	 * Topic name is formed from the PID_TOPIC_NAME parameter
	 * 
	 * @return topicName
	 */
	public String getTopicName() {
		return topicName;
	}

	/**
	 * Gets the GUID_t of the entity represented by this DiscoveredData.
	 * GUID_t is formed from the PID_KEY_HASH parameter.
	 * 
	 * @return key as GUID_t
	 */
	public GUID_t getKey() {
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
	 * Adds a QosPolicy.
	 * @param policy
	 */
	protected void addQosPolicy(QosPolicy policy) {
		try {
			qos.setPolicy(policy);
		} catch (InconsistentPolicy e) {
			inconsistenPolicies.add(policy);
		}
	}
	
	/**
	 * Get inlineable Qos policies. Such policies implement InlineParameter.
	 * 
	 * @return QosPolicies that can be inlined to Data
	 * @see net.sf.jrtps.message.Data#getInlineQos()
	 * @see InlineParameter
	 */
	public Set<QosPolicy> getInlineableQosPolicies() {
		return qos.getInlinePolicies();
	}

	public String toString() {
		return topicName + "(" + typeName + "): " + key  + ", QoS: " + qos;
	}
}
