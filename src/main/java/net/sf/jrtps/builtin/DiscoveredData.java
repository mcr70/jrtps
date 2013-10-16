package net.sf.jrtps.builtin;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.message.parameter.QualityOfService;
import net.sf.jrtps.types.GUID_t;
import net.sf.jrtps.types.Locator_t;

public class DiscoveredData {
	private Locator_t locator;
	protected String typeName;
	protected String topicName;
	// On spec, key is BuiltinTopicKey_t (4 bytes), but KeyHash Parameter is 16 bytes.
	// @see table 9.10, 9.6.3.3 KeyHash.
	// interpretation is, for builtin topics, key is 
	//   guid_prefix(12) + builtin_topic_key(4), which is equal to prefix(12) + entityid(4), which is guid
	protected GUID_t key;  
	protected List<QualityOfService> qosList = new LinkedList<>();
	
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
	 * Get the type name that is associated with this DiscovereData.
	 * Topic name is formed from the PID_TYPE_NAME parameter
	 * @return
	 */
	public String getTypeName() {
		return typeName;
	}
	
	/**
	 * Get the topic name that is associated with this DiscovereData.
	 * Topic name is formed from the PID_TOPIC_NAME parameter
	 * @return
	 */
	public String getTopicName() {
		return topicName;
	}

	/**
	 * Gets the GUID_t of the entity represented by this DiscoveredData.
	 * GUID_t is formed from the PID_KEY_HASH parameter.
	 * 
	 * @return
	 */
	public GUID_t getKey() {
		return key;
	}
	
	public void addQualityOfService(QualityOfService qos) {
		// TODO: reader, writer & topics have different set of QualityOfServices.
		qosList.add(qos);
	}
	
	public List<QualityOfService> getQualityOfServices() {
		return qosList;
	}

	public String toString() {
		return topicName + "(" + typeName + "): " + key  + ", QoS: " + getQualityOfServices();
	}
	
	public Locator_t getLocator() {
		return locator;
	}

	public void setLocator(Locator_t locator) {
		this.locator = locator;
	}
}
