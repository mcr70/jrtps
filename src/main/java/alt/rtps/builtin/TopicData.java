package alt.rtps.builtin;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.message.parameter.Parameter;
import alt.rtps.message.parameter.ParameterList;
import alt.rtps.message.parameter.QualityOfService;
import alt.rtps.message.parameter.TopicName;
import alt.rtps.message.parameter.TypeName;
import alt.rtps.types.GUID_t;

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
				if (param instanceof QualityOfService) {
					addQualityOfService((QualityOfService) param);
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
