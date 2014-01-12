package net.sf.jrtps.udds;

import java.util.HashMap;
import java.util.List;

import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.Sample;
import net.sf.jrtps.SampleListener;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.ReaderData;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BuiltinReaderDataListener extends BuiltinListener implements SampleListener<ReaderData> {
	private static final Logger log = LoggerFactory.getLogger(BuiltinReaderDataListener.class);

	private HashMap<GuidPrefix, ParticipantData> discoveredParticipants;
	private HashMap<Guid, ReaderData> discoveredReaders;

	BuiltinReaderDataListener(Participant p, HashMap<GuidPrefix, ParticipantData> discoveredParticipants, HashMap<Guid, ReaderData> discoveredReaders) {
		super(p);
		this.discoveredParticipants = discoveredParticipants;
		this.discoveredReaders = discoveredReaders;
	}

	/**
	 * Handle discovered ReaderData. If ReaderData represents an user defined
	 * Reader, and this participant has a Writer for same topic, send writers history
	 * cache to reader.
	 * 
	 * @param samples
	 */
	@Override
	public void onSamples(List<Sample<ReaderData>> samples) {
		for (Sample<ReaderData> rdSample : samples) {
			ReaderData readerData = rdSample.getData();
			//discoveredReaders.put(readerData.getParticipantGuid(), readerData);
			Guid key = readerData.getKey();
			if (discoveredReaders.put(key, readerData) == null) {
				log.debug("Discovered a new reader {} for topic {}, type {}", key, readerData.getTopicName(), readerData.getTypeName());
				fireReaderDetected(readerData);
			}

			List<DataWriter<?>> writers = participant.getWritersForTopic(readerData.getTopicName());
			for (DataWriter<?> w : writers) {
				if (rdSample.isDisposed()) {
					w.getRTPSWriter().removeMatchedReader(readerData);
				}
				else {
					QualityOfService requested = readerData.getQualityOfService();
					QualityOfService offered = w.getRTPSWriter().getQualityOfService();
					log.debug("Check for compatible QoS for {} and {}", w.getRTPSWriter().getGuid().getEntityId(), readerData.getKey().getEntityId());

					if (offered.isCompatibleWith(requested)) {
						w.getRTPSWriter().addMatchedReader(readerData);
						fireReaderMatched(w, readerData);
					}
					else {
						log.warn("Discovered reader had incompatible QoS with writer. {}, {}", readerData, w);
						fireInconsistentQoS(w, readerData);
					}					
				}

				// builtin entities are handled with SEDP in ParticipantData reception
				// TODO: user-defined entities should not be handled differently.
				if (key.getEntityId().isUserDefinedEntity()) {  
					ParticipantData pd = discoveredParticipants.get(key.getPrefix());
					if (pd != null) {
						//w.getRTPSWriter().sendData(key.getPrefix(), key.getEntityId(), 0L);
						log.debug("Notify reader {}", key.getEntityId());
						w.getRTPSWriter().notifyReader(key);
					}
					else {
						log.warn("Participant was not found: {}", key.getPrefix());
					}
				}
			}
		}
	}
}
