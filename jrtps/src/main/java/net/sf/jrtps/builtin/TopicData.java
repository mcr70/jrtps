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

public class TopicData extends DiscoveredData {
	private static final Logger log = LoggerFactory.getLogger(TopicData.class);
	

	public TopicData(ParameterList paramterList) {
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
					addQualityOfService((QosPolicy) param);
				}
				else {
					log.warn("Parameter {} not handled", param.getParameterId());
				}
			}

		}
	}

	public TopicData(String typeName, String topicName, GUID_t key) {
		super(typeName, topicName, key);
	}

}
