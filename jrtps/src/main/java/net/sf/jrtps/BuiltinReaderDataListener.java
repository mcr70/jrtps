package net.sf.jrtps;

import java.util.HashMap;
import java.util.List;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.ReaderData;
import net.sf.jrtps.types.GUID_t;
import net.sf.jrtps.types.GuidPrefix_t;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BuiltinReaderDataListener implements SampleListener<ReaderData> {
	private static final Logger log = LoggerFactory.getLogger(BuiltinReaderDataListener.class);

	private final RTPSParticipant participant;
	private HashMap<GuidPrefix_t, ParticipantData> discoveredParticipants;
	private HashMap<GUID_t, ReaderData> discoveredReaders;

	BuiltinReaderDataListener(RTPSParticipant p, HashMap<GuidPrefix_t, ParticipantData> discoveredParticipants, HashMap<GUID_t, ReaderData> discoveredReaders) {
		this.participant = p;
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
			GUID_t key = readerData.getKey();
			if (discoveredReaders.put(key, readerData) == null) {
				log.debug("Discovered a new reader {} for topic {}, type {}", key, readerData.getTopicName(), readerData.getTypeName());
			}

			List<RTPSWriter<?>> writers = participant.getWritersForTopic(readerData.getTopicName());
			for (RTPSWriter<?> w : writers) {
				if (rdSample.isDisposed()) {
					w.removeMatchedReader(readerData);
				}
				else {
					QualityOfService requested = readerData.getQualityOfService();
					QualityOfService offered = w.getQualityOfService();
					log.debug("Check for compatible QoS for {} and {}", w.getGuid().entityId, readerData.getKey().entityId);

					if (offered.isCompatibleWith(requested)) {
						w.addMatchedReader(readerData);
					}
					else {
						log.warn("Discovered reader had incompatible QoS with writer. {}, {}", readerData, w);
					}					
				}

				// builtin entities are handled with SEDP in ParticipantData reception
				// TODO: user-defined entities should not be handled differently.
				if (key.entityId.isUserDefinedEntity()) {  
					ParticipantData pd = discoveredParticipants.get(key.prefix);
					if (pd != null) {
						w.sendData(key.prefix, key.entityId, 0L);
					}
					else {
						log.warn("Participant was not found: {}", key.prefix);
					}
				}
			}
		}
	}
}
