package net.sf.jrtps.udds;

import java.util.List;
import java.util.Map;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.Sample;
import net.sf.jrtps.SampleListener;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.types.Guid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BuiltinPublicationDataListener extends BuiltinListener implements SampleListener<PublicationData> {
	private static final Logger log = LoggerFactory.getLogger(BuiltinPublicationDataListener.class);

	private Map<Guid, PublicationData> discoveredWriters;

	BuiltinPublicationDataListener(Participant p, Map<Guid, PublicationData> discoveredWriters) {
		super(p);
		this.discoveredWriters = discoveredWriters;
	}

	@Override
	public void onSamples(List<Sample<PublicationData>> samples) {
		for (Sample<PublicationData> pdSample : samples) {
			PublicationData pd = pdSample.getData();

			Guid key = pd.getKey();
			if (discoveredWriters.put(key, pd) == null) {
				log.debug("Discovered a new publication {} for topic {}, type {}", key, pd.getTopicName(), pd.getTypeName());
				fireWriterDetected(pd);
			}

			List<DataReader<?>> readers = participant.getReadersForTopic(pd.getTopicName());
			for (DataReader<?> r : readers) {
				if (!r.getRTPSReader().isMatchedWith(pd) && !pdSample.isDisposed()) {
					// Not associated and sample is not a dispose -> do associate
					QualityOfService offered = pd.getQualityOfService();
					QualityOfService requested = r.getRTPSReader().getQualityOfService();
					log.trace("Check for compatible QoS for {} and {}", pd.getKey().getEntityId(), r.getRTPSReader().getGuid().getEntityId());

					if (offered.isCompatibleWith(requested)) {
						r.getRTPSReader().addMatchedWriter(pd);
						fireWriterMatched(r, pd);
					}
					else {
						log.warn("Discovered writer had incompatible QoS with reader. {}, {}", pd, r.getRTPSReader().getQualityOfService());
						fireInconsistentQoS(r, pd);
					}
				}
				else if (r.getRTPSReader().isMatchedWith(pd) && pdSample.isDisposed()) {
					// Associated and sample is dispose -> remove association
					r.getRTPSReader().removeMatchedWriter(pd);
				}
			}
		}
	}
}
