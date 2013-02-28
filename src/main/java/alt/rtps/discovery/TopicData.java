package alt.rtps.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.message.parameter.KeyHash;
import alt.rtps.message.parameter.Parameter;
import alt.rtps.message.parameter.QualityOfService;
import alt.rtps.message.parameter.TopicName;
import alt.rtps.message.parameter.TypeName;
import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.BuiltinTopicKey_t;

public class TopicData extends DiscoveredData {
	private static final Logger log = LoggerFactory.getLogger(TopicData.class);

	public TopicData(KeyHash keyHash, RTPSByteBuffer buffer) {
		boolean moreParameters = buffer.getBuffer().remaining() > 0; //true;
		while (moreParameters) {
			Parameter param = Parameter.readParameter(buffer);

			log.debug("{}", param);
			switch(param.getParameterId()) {
			case PID_TOPIC_NAME:
				super.topicName = ((TopicName)param).getName();
				break;
			case PID_TYPE_NAME:
				super.typeName = ((TypeName)param).getTypeName();
				break;
			default:
				if (param instanceof QualityOfService) {
					addQualityOfService((QualityOfService) param);
				}
				else {
					log.warn("Parameter {} not handled", param.getParameterId());
				}
			}

		}
	}

	public TopicData(String typeName, String topicName, BuiltinTopicKey_t key) {
		super(typeName, topicName, key);
	}
}
