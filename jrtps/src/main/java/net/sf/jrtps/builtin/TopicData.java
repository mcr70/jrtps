package net.sf.jrtps.builtin;

import java.util.Iterator;

import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.message.parameter.Parameter;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.QosPolicy;
import net.sf.jrtps.message.parameter.TopicName;
import net.sf.jrtps.message.parameter.TypeName;
import net.sf.jrtps.types.GUID_t;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopicData extends DiscoveredData {
	public static final String BUILTIN_TOPIC_NAME = "DCPSTopic";
	
	private static final Logger log = LoggerFactory.getLogger(TopicData.class);

	public TopicData(ParameterList paramterList) throws InconsistentPolicy {
		Iterator<Parameter> iter = paramterList.getParameters().iterator();
		while (iter.hasNext()) {
			Parameter param = iter.next();

			log.debug("{}", param);
			switch(param.getParameterId()) {
			case PID_TOPIC_NAME:
				super.topicName = ((TopicName)param).getName();
				break;
			case PID_TYPE_NAME:
				super.typeName = ((TypeName)param).getTypeName();
				break;
			default:
				if (param instanceof QosPolicy) {
					addQosPolicy((QosPolicy) param);
				}
				else {
					log.warn("Parameter {} not handled", param.getParameterId());
				}
			}
		}
		
		if (super.typeName == null) { // Other vendors may use different typeName
			super.typeName = TopicData.class.getName();
		}
		
		resolveInconsistencies();
	}

	public TopicData(String typeName, String topicName, GUID_t key) {
		super(typeName, topicName, key);
	}
}
