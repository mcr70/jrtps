package net.sf.jrtps;

import java.util.HashMap;

import net.sf.jrtps.builtin.WriterData;
import net.sf.jrtps.message.parameter.StatusInfo;
import net.sf.jrtps.types.GUID_t;
import net.sf.jrtps.types.Time_t;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuiltinWriterDataListener implements DataListener<WriterData>{
	private static final Logger log = LoggerFactory.getLogger(BuiltinWriterDataListener.class);

	private final RTPSParticipant participant;
	private HashMap<GUID_t, WriterData> discoveredWriters;

	BuiltinWriterDataListener(RTPSParticipant p, HashMap<GUID_t, WriterData> discoveredWriters) {
		this.participant = p;
		this.discoveredWriters = discoveredWriters;
	}
	
	@Override
	public void onData(WriterData writerData, Time_t timestamp, StatusInfo sInfo) {
		GUID_t key = writerData.getKey();
		if (discoveredWriters.put(key, writerData) == null) {
			log.debug("Discovered a new writer {} for topic {}, type {}", key, writerData.getTopicName(), writerData.getTypeName());
		}

		RTPSReader r = participant.getReaderForTopic(writerData.getTopicName());
		if (r != null) {
			if (sInfo.isDisposed()) {
				r.removeMatchedWriter(writerData);
			}
			else {
				r.addMatchedWriter(writerData);
			}
		}
	}
}
