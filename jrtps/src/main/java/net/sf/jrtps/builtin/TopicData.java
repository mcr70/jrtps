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

public class TopicData extends DiscoveredData {
	public static final String BUILTIN_TOPIC_NAME = "DCPSTopic";
	public static final String BUILTIN_TYPE_NAME = "TOPIC_BUILT_IN_TOPIC_TYPE"; // TODO: for odds 3.5
	
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

	public TopicData(String typeName, String topicName, Guid key, QualityOfService qos) {
		super(typeName, topicName, key, qos);
	}
}
