package net.sf.jrtps;

import java.util.List;

import net.sf.jrtps.builtin.TopicData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BuiltinTopicDataListener implements SampleListener<TopicData> {
	private static final Logger log = LoggerFactory.getLogger(BuiltinTopicDataListener.class);
	
	public BuiltinTopicDataListener(RTPSParticipant participant) {
	}
	
	@Override
	public void onSamples(List<Sample<TopicData>> samples) {
		log.debug("TopicData is not handled");
	}
}
