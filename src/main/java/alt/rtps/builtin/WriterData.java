package alt.rtps.builtin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.message.parameter.KeyHash;
import alt.rtps.message.parameter.Parameter;
import alt.rtps.message.parameter.ParameterFactory;
import alt.rtps.message.parameter.QualityOfService;
import alt.rtps.message.parameter.TopicName;
import alt.rtps.message.parameter.TypeName;
import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.GUID_t;

public class WriterData extends DiscoveredData {
	private static final Logger log = LoggerFactory.getLogger(WriterData.class);
	
	public WriterData(RTPSByteBuffer buffer) {
		boolean moreParameters = buffer.getBuffer().remaining() > 0; //true;
		while (moreParameters) {
			Parameter param = ParameterFactory.readParameter(buffer);

			log.trace("{}", param);
			switch(param.getParameterId()) {
			case PID_PROTOCOL_VERSION:
			case PID_VENDORID:
			case PID_VENDOR_SPECIFIC:
				// These parameters get sent by OSPL 5.5. We can ignore these
				break;
			case PID_TOPIC_NAME:
				super.topicName = ((TopicName)param).getName(); break;
			case PID_TYPE_NAME:
				super.typeName = ((TypeName)param).getTypeName(); break;
			case PID_KEY_HASH:
				super.keyHash = (KeyHash) param; break;
			case PID_SENTINEL:
				moreParameters = false; break;
			case PID_PAD:
				// Ignore
				break;

			default:
				if (param instanceof QualityOfService) {
					addQualityOfService((QualityOfService) param);
				}
				else {
					log.warn("Parameter {} not handled: {}", param.getParameterId(), param);
				}
			}
		}
	}
	
	public WriterData(String topicName, String typeName, GUID_t key) {
		super(typeName, topicName, key);
	}
}
