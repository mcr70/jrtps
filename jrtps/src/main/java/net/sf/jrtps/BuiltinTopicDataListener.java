package net.sf.jrtps;

import net.sf.jrtps.builtin.TopicData;
import net.sf.jrtps.message.parameter.StatusInfo;
import net.sf.jrtps.types.Time_t;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuiltinTopicDataListener implements DataListener<TopicData> {
	private static final Logger log = LoggerFactory.getLogger(BuiltinTopicDataListener.class);
	
	public BuiltinTopicDataListener(RTPSParticipant participant) {
	}
	
	@Override
	public void onData(TopicData topicData, Time_t timestamp, StatusInfo sInfo) {
		log.debug("TopicData is not handled");
	}
}
