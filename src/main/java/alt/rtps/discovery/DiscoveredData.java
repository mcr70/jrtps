package alt.rtps.discovery;

import java.util.LinkedList;
import java.util.List;

import alt.rtps.message.parameter.KeyHash;
import alt.rtps.message.parameter.QualityOfService;
import alt.rtps.types.BuiltinTopicKey_t;
import alt.rtps.types.GUID_t;

public class DiscoveredData {
	protected String typeName;
	protected String topicName;
	protected BuiltinTopicKey_t key;
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
	 * @param topicKey
	 */
	protected DiscoveredData(String typeName, String topicName, BuiltinTopicKey_t topicKey) {
		this.typeName = typeName;
		this.topicName = topicName;
		this.key = topicKey;
	}
	
	public String getTypeName() {
		return typeName;
	}
	
	public String getTopicName() {
		return topicName;
	}
	
	public BuiltinTopicKey_t getKey() {
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
