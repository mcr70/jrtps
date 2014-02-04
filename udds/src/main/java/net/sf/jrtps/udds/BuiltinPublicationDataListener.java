package net.sf.jrtps.udds;

import java.util.List;
import java.util.Map;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.Sample;
import net.sf.jrtps.SampleListener;
import net.sf.jrtps.WriterProxy;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BuiltinPublicationDataListener extends BuiltinListener implements SampleListener<PublicationData> {
	private static final Logger log = LoggerFactory.getLogger(BuiltinPublicationDataListener.class);

	private Map<GuidPrefix, ParticipantData> discoveredParticipants;
	private Map<Guid, PublicationData> discoveredWriters;

	BuiltinPublicationDataListener(Participant p, Map<GuidPrefix, ParticipantData> discoveredParticipants, Map<Guid, PublicationData> discoveredWriters) {
		super(p);
		this.discoveredParticipants = discoveredParticipants; 
		this.discoveredWriters = discoveredWriters;
	}

	@Override
	public void onSamples(List<Sample<PublicationData>> samples) {
		for (Sample<PublicationData> wdSample : samples) {
			PublicationData writerData = wdSample.getData();

			Guid key = writerData.getKey();
			if (discoveredWriters.put(key, writerData) == null) {
				log.debug("Discovered a new writer {} for topic {}, type {}", key, writerData.getTopicName(), writerData.getTypeName());
				fireWriterDetected(writerData);
			}

			List<DataReader<?>> readers = participant.getReadersForTopic(writerData.getTopicName());
			for (DataReader<?> r : readers) {
				if (!r.getRTPSReader().isMatchedWith(writerData)) {
					if (wdSample.isDisposed()) {
						r.getRTPSReader().removeMatchedWriter(writerData);
					}
					else {
						QualityOfService offered = writerData.getQualityOfService();
						QualityOfService requested = r.getRTPSReader().getQualityOfService();
						log.trace("Check for compatible QoS for {} and {}", writerData.getKey().getEntityId(), r.getRTPSReader().getGuid().getEntityId());

						if (offered.isCompatibleWith(requested)) {
							WriterProxy proxy = r.getRTPSReader().addMatchedWriter(writerData);
							fireWriterMatched(r, writerData);
						}
						else {
							log.warn("Discovered writer had incompatible QoS with reader. {}, {}", writerData, r.getRTPSReader().getQualityOfService());
							fireInconsistentQoS(r, writerData);
						}
					}
				}
			}
		}
	}
}
