package net.sf.jrtps;

import java.util.HashMap;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.ReaderData;
import net.sf.jrtps.message.parameter.StatusInfo;
import net.sf.jrtps.types.GUID_t;
import net.sf.jrtps.types.GuidPrefix_t;
import net.sf.jrtps.types.Time_t;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuiltinReaderDataListener implements DataListener<ReaderData> {
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
	 * @param readerData
	 */
	@Override
	public void onData(ReaderData readerData, Time_t timestamp, StatusInfo sInfo) {
		//discoveredReaders.put(readerData.getParticipantGuid(), readerData);
		GUID_t key = readerData.getKey();
		if (discoveredReaders.put(key, readerData) == null) {
			log.debug("Discovered a new reader {} for topic {}, type {}", key, readerData.getTopicName(), readerData.getTypeName());
		}

		RTPSWriter writer = participant.getWriterForTopic(readerData.getTopicName());
		if (writer != null) {
			if (sInfo.isDisposed()) {
				writer.removeMatchedReader(readerData);
			}
			else {
				writer.addMatchedReader(readerData);
			}
		}

		// builtin entities are handled with SEDP in ParticipantData reception
		if (key.entityId.isUserDefinedEntity() && writer != null) {  
			ParticipantData pd = discoveredParticipants.get(key.prefix);
			if (pd != null) {
				writer.sendHistoryCache(pd.getUnicastLocator(), key.entityId);
			}
			else {
				log.warn("Participant was not found: {}", key.prefix);
			}
		}
	}
}
