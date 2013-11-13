package net.sf.jrtps.builtin;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.message.parameter.InlineParameter;
import net.sf.jrtps.message.parameter.QosPolicy;
import net.sf.jrtps.types.GUID_t;

public class DiscoveredData {
	protected String typeName;
	protected String topicName;
	// On spec, key is BuiltinTopicKey_t (4 bytes), but KeyHash Parameter is 16 bytes.
	// see table 9.10, 9.6.3.3 KeyHash.
	// interpretation is, for builtin topics, key is 
	//   guid_prefix(12) + builtin_topic_key(4), which is equal to prefix(12) + entityid(4), which is guid
	protected GUID_t key;  
	protected List<QosPolicy> qosPolicyList = new LinkedList<>();
	
	/**
	 * This constructor is used when DiscoveredData is being created from 
	 * RTPSByteBuffer
	 */
	protected DiscoveredData() {
	}
	
	/**
	 * This constructor is used when DiscoveredData is being created from scratch
	 * 
	 * @param typeName
	 * @param topicName
	 * @param key
	 */
	protected DiscoveredData(String typeName, String topicName, GUID_t key) {
		this.typeName = typeName;
		this.topicName = topicName;
		this.key = key;
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
	 * Adds a QosPolicy.
	 * @param qos
	 */
	protected void addQosPolicy(QosPolicy qos) {
		// TODO: reader, writer & topics have different set of QualityOfServices.
		//       create tagging interfaces: Reader/Writer/TopicPolicy extends QosPolicy
		qosPolicyList.add(qos);
	}
	
	/**
	 * Get the QosPolicies.
	 * @return a List of QosPolicies
	 */
	public List<QosPolicy> getQosPolicies() {
		return qosPolicyList;
	}

	/**
	 * Get inlineable Qos policies. Such policies implement InlineParameter.
	 * 
	 * @return QosPolicies that can be inlined to Data
	 * @see net.sf.jrtps.message.Data#getInlineQos()
	 * @see InlineParameter
	 */
	public List<QosPolicy> getInlineableQosPolicies() {
		List<QosPolicy> inlineQos = new LinkedList<>();
		for (QosPolicy qp : qosPolicyList) {
			if (qp instanceof InlineParameter) {
				inlineQos.add(qp);
			}
		}
		
		return inlineQos;
	}
	
	public String toString() {
		return topicName + "(" + typeName + "): " + key  + ", QoS: " + getQosPolicies();
	}
}
