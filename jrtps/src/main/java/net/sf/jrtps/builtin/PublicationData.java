package net.sf.jrtps.builtin;

import java.util.Iterator;

import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.message.parameter.Parameter;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.QosPolicy;
import net.sf.jrtps.message.parameter.TopicName;
import net.sf.jrtps.message.parameter.TypeName;
import net.sf.jrtps.types.Guid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents DDS::PublicationBuiltinTopicData
 * 
 * @author mcr70
 *
 */
public class PublicationData extends DiscoveredData {
	public static final String BUILTIN_TOPIC_NAME = "DCPSPublication";
	
	private static final Logger log = LoggerFactory.getLogger(PublicationData.class);
	
	public PublicationData(ParameterList parameterList) throws InconsistentPolicy {
		Iterator<Parameter> iter = parameterList.getParameters().iterator();
		while (iter.hasNext()) {
			Parameter param = iter.next();
			addParameter(param);

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
				super.key = new Guid(param.getBytes()); break;
			case PID_SENTINEL:
				break;
			case PID_PAD:
				// Ignore
				break;

			default:
				if (param instanceof QosPolicy) {
					addQosPolicy((QosPolicy) param);
				}
			}
		}
		
		if (super.typeName == null) { // Other vendors may use different typeName
			super.typeName = PublicationData.class.getName();
		}
		
		// Resolve possible inconsistencies
		resolveInconsistencies();
	}

	public PublicationData(String topicName, String typeName, Guid key, QualityOfService qos) {
		super(typeName, topicName, key, qos);
	}
}
