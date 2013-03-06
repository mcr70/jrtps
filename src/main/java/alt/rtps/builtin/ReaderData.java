package alt.rtps.builtin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.message.parameter.KeyHash;
import alt.rtps.message.parameter.Parameter;
import alt.rtps.message.parameter.ParameterFactory;
import alt.rtps.message.parameter.ParticipantGuid;
import alt.rtps.message.parameter.QualityOfService;
import alt.rtps.message.parameter.TopicName;
import alt.rtps.message.parameter.TypeName;
import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.BuiltinTopicKey_t;
import alt.rtps.types.ContentFilterProperty_t;
import alt.rtps.types.GUID_t;

public class ReaderData extends DiscoveredData {
	private static final Logger log = LoggerFactory.getLogger(ReaderData.class);

	private GUID_t readerGuid;
	private boolean expectsInlineQos = false;
	
	private ContentFilterProperty_t contentFilter;
	
	public ReaderData(RTPSByteBuffer buffer) {
		boolean moreParameters = buffer.getBuffer().remaining() > 0; //true;
		while (moreParameters) {
			Parameter param = ParameterFactory.readParameter(buffer);

			log.trace("{}", param);
			switch(param.getParameterId()) {
			case PID_PROTOCOL_VERSION:
			case PID_VENDORID:
				// These parameters get sent by OSPL 5.5. We can ignore these
				break;
			case PID_TOPIC_NAME:
				super.topicName = ((TopicName)param).getName();
				break;
			case PID_PARTICIPANT_GUID:
				readerGuid = ((ParticipantGuid)param).getParticipantGuid();
				break;
			case PID_TYPE_NAME:
				super.typeName = ((TypeName)param).getTypeName();
				break;
			case PID_KEY_HASH:
				keyHash = (KeyHash) param; break;
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
					log.warn("Parameter {} not handled", param.getParameterId());
				}
			}
		}
	}

	public ReaderData(String topicName, String typeName, BuiltinTopicKey_t key) {
		super(typeName, topicName, key);
	}
	
	public GUID_t getReaderGuid() {
		return readerGuid;
	}
	
	public boolean expectsInlineQos() {
		return expectsInlineQos;
	}
	
	public ContentFilterProperty_t getContentFilter() {
		return contentFilter;
	}
}
