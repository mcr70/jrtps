package net.sf.jrtps.builtin;

import java.util.Iterator;

import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.Parameter;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.ParticipantGuid;
import net.sf.jrtps.message.parameter.QosPolicy;
import net.sf.jrtps.message.parameter.TopicName;
import net.sf.jrtps.message.parameter.TypeName;
import net.sf.jrtps.types.ContentFilterProperty_t;
import net.sf.jrtps.types.Guid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents DDS::SubscriptionBuiltinTopicData
 * 
 * @author mcr70
 */
public class SubscriptionData extends DiscoveredData {
	public static final String BUILTIN_TOPIC_NAME = "DCPSSubscription";
	
	private static final Logger log = LoggerFactory.getLogger(SubscriptionData.class);

	private Guid participantGuid;
	private boolean expectsInlineQos = false;
	
	private ContentFilterProperty_t contentFilter;
	
	public SubscriptionData(ParameterList parameterList) throws InconsistentPolicy {
		Iterator<Parameter> iter = parameterList.getParameters().iterator();
		while (iter.hasNext()) {
			Parameter param = iter.next();

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
				participantGuid = ((ParticipantGuid)param).getParticipantGuid();
				break;
			case PID_TYPE_NAME:
				super.typeName = ((TypeName)param).getTypeName();
				break;
			case PID_KEY_HASH:
				//keyHash = (KeyHash) param;
				super.key = new Guid(param.getBytes()); // TODO: We should store either GUID, or KeyHash only
				break;
			case PID_SENTINEL:
				break;
			case PID_PAD:
				// Ignore
				break;
				
			default:
				if (param instanceof QosPolicy) {
					addQosPolicy((QosPolicy) param);
				}
				else {
					log.warn("Parameter {} not handled: {}", param.getParameterId(), param);
				}
			}
		}
		
		if (super.typeName == null) { // Other vendors may use different typeName
			super.typeName = SubscriptionData.class.getName();
		}
		
		resolveInconsistencies();
	}

	public SubscriptionData(String topicName, String typeName, Guid key, QualityOfService qos) {
		super(typeName, topicName, key, qos);
	}
	
	public Guid getParticipantGuid() {
		return participantGuid;
	}
	
	public boolean expectsInlineQos() {
		return expectsInlineQos;
	}
	
	public ContentFilterProperty_t getContentFilter() {
		return contentFilter;
	}
}
