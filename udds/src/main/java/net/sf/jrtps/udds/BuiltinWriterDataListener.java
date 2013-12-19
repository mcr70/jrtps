package net.sf.jrtps.udds;

import java.util.HashMap;
import java.util.List;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.RTPSParticipant;
import net.sf.jrtps.RTPSReader;
import net.sf.jrtps.Sample;
import net.sf.jrtps.SampleListener;
import net.sf.jrtps.builtin.WriterData;
import net.sf.jrtps.types.Guid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BuiltinWriterDataListener implements SampleListener<WriterData>{
	private static final Logger log = LoggerFactory.getLogger(BuiltinWriterDataListener.class);

	private final Participant participant;
	private HashMap<Guid, WriterData> discoveredWriters;

	BuiltinWriterDataListener(Participant p, HashMap<Guid, WriterData> discoveredWriters) {
		this.participant = p;
		this.discoveredWriters = discoveredWriters;
	}

	@Override
	public void onSamples(List<Sample<WriterData>> samples) {
		for (Sample<WriterData> wdSample : samples) {
			WriterData writerData = wdSample.getData();

			Guid key = writerData.getKey();
			if (discoveredWriters.put(key, writerData) == null) {
				log.debug("Discovered a new writer {} for topic {}, type {}", key, writerData.getTopicName(), writerData.getTypeName());
			}

			List<DataReader<?>> readers = participant.getReadersForTopic(writerData.getTopicName());
			for (DataReader<?> r : readers) {
				if (r != null) {
					if (wdSample.isDisposed()) {
						r.getRTPSReader().removeMatchedWriter(writerData);
					}
					else {
						QualityOfService offered = writerData.getQualityOfService();
						QualityOfService requested = r.getRTPSReader().getQualityOfService();
						log.debug("Check for compatible QoS for {} and {}", writerData.getKey().entityId, r.getRTPSReader().getGuid().entityId);
						
						if (offered.isCompatibleWith(requested)) {
							r.getRTPSReader().addMatchedWriter(writerData);
						}
						else {
							log.warn("Discovered writer had incompatible QoS with reader. {}, {}", writerData, r);
						}
					}
				}
			}
		}
	}
}
