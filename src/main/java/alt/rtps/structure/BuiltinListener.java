package alt.rtps.structure;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.builtin.ParticipantData;
import alt.rtps.builtin.ReaderData;
import alt.rtps.builtin.TopicData;
import alt.rtps.builtin.WriterData;
import alt.rtps.types.BuiltinTopicKey_t;
import alt.rtps.types.GUID_t;
import alt.rtps.types.GuidPrefix_t;
import alt.rtps.types.Time_t;

/**
 * BuiltinListener keeps track of remote entities.
 * 
 * @author mcr70
 *
 */
class BuiltinListener implements DataListener {
	private static final Logger log = LoggerFactory.getLogger(BuiltinListener.class);
	private Participant participant;

	private final HashMap<GuidPrefix_t, ParticipantData> discoveredParticipants;
	private final HashMap<BuiltinTopicKey_t, TopicData> discoveredTopics = new HashMap<>();
	private final HashMap<GUID_t, ReaderData> discoveredReaders = new HashMap<>();
	private final HashMap<GUID_t, WriterData> discoveredWriters = new HashMap<>();

	
	BuiltinListener(Participant p, HashMap<GuidPrefix_t, ParticipantData> discoveredParticipants) {
		this.participant = p;
		this.discoveredParticipants = discoveredParticipants;
	}
	
	@Override
	public void onData(Object data, Time_t timestamp) {
		if (data instanceof ParticipantData) {	
			
			ParticipantData pd = (ParticipantData) data;
			log.debug("Considering Participant {}", pd.getGuid());
			
			ParticipantData d = discoveredParticipants.get(pd.getGuidPrefix());
			if (d == null && pd.getGuidPrefix() != null) {
				if (pd.getGuidPrefix().equals(participant.guid.prefix)) {
					log.trace("Ignoring self");
				}
				else {
					log.debug("A new Participant detected: {}", pd); //.getGuidPrefix() + ", " + pd.getAllLocators());
					discoveredParticipants.put(pd.getGuidPrefix(), pd);
				}
			}
		}
		else if (data instanceof WriterData) {
			WriterData writerData = (WriterData) data;
			discoveredWriters.put(writerData.getWriterGuid(), writerData);
		}
		else if (data instanceof ReaderData) {
			ReaderData readerData = (ReaderData) data;			
			discoveredReaders.put(readerData.getReaderGuid(), readerData);
		}
		else if (data instanceof TopicData) {
			TopicData topicData = (TopicData) data;
			discoveredTopics.put(topicData.getKey(), topicData);
		}
	}
}
