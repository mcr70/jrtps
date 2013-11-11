package net.sf.jrtps.builtin;

import java.util.Iterator;

import net.sf.jrtps.message.parameter.Parameter;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.QosPolicy;
import net.sf.jrtps.message.parameter.TopicName;
import net.sf.jrtps.message.parameter.TypeName;
import net.sf.jrtps.types.GUID_t;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WriterData extends DiscoveredData {
	private static final Logger log = LoggerFactory.getLogger(WriterData.class);
	
	public WriterData(ParameterList parameterList) {
		Iterator<Parameter> iter = parameterList.getParameters().iterator();
		while (iter.hasNext()) {
			Parameter param = iter.next();

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
				//super.keyHash = (KeyHash) param; 
				super.key = new GUID_t(param.getBytes()); // TODO: We should store either GUID, or KeyHash only

				break;
			case PID_SENTINEL:
				break;
			case PID_PAD:
				// Ignore
				break;

			default:
				if (param instanceof QosPolicy) {
					addQualityOfService((QosPolicy) param);
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
