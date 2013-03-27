package alt.rtps.builtin;

import java.util.LinkedList;
import java.util.List;

import alt.rtps.message.parameter.KeyHash;
import alt.rtps.message.parameter.QualityOfService;
import alt.rtps.types.GUID_t;

public class DiscoveredData {
	protected String typeName;
	protected String topicName;
	// On spec, key is BuiltinTopicKey_t (4 bytes), but KeyHash Parameter is 16 bytes.
	// @see table 9.10, 9.6.3.3 KeyHash.
	// interpretation is, for builtin topics, key is 
	//   guid_prefix(12) + builtin_topic_key(4), which is equal to prefix(12) + entityid(4), which is guid
	protected GUID_t key;  
	protected KeyHash keyHash;
	protected List<QualityOfService> qosList = new LinkedList<>();
	
	private GUID_t writerGuid;
	
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
		this.keyHash = new KeyHash(key.getBytes());
	}
	
	public String getTypeName() {
		return typeName;
	}
	
	public String getTopicName() {
		return topicName;
	}
	
	public GUID_t getKey() {
		// TODO: Should we have getKey()  -> BuiltinTopicKey_t and
		//                      getGuid() -> key
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
		return topicName + "(" + typeName + "): " + keyHash  + ", QoS: " + getQualityOfServices();
	}

	public GUID_t getWriterGuid() {
		return writerGuid;
	}

	public void setWriterGuid(GUID_t writerGuid) {
		this.writerGuid = writerGuid;
	}
}
