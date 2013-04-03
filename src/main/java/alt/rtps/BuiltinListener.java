package alt.rtps;

import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.builtin.ParticipantData;
import alt.rtps.builtin.ReaderData;
import alt.rtps.builtin.TopicData;
import alt.rtps.builtin.WriterData;
import alt.rtps.message.Heartbeat;
import alt.rtps.message.parameter.BuiltinEndpointSet;
import alt.rtps.types.BuiltinEndpointSet_t;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.GUID_t;
import alt.rtps.types.GuidPrefix_t;
import alt.rtps.types.Locator_t;
import alt.rtps.types.Time_t;

/**
 * BuiltinListener keeps track of remote entities.
 * 
 * @author mcr70
 *
 */
class BuiltinListener implements DataListener {
	private static final Logger log = LoggerFactory.getLogger(BuiltinListener.class);
	private RTPSParticipant participant;

	private final HashMap<GuidPrefix_t, ParticipantData> discoveredParticipants;
	private final HashMap<GUID_t, TopicData> discoveredTopics = new HashMap<>();
	private final HashMap<GUID_t, ReaderData> discoveredReaders = new HashMap<>();
	private final HashMap<GUID_t, WriterData> discoveredWriters = new HashMap<>();


	BuiltinListener(RTPSParticipant p, HashMap<GuidPrefix_t, ParticipantData> discoveredParticipants) {
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

					handleBuiltinEnpointSet(pd.getBuiltinEndpoints(), pd.getMetatrafficUnicastLocator());
				}
			}
		}
		else if (data instanceof WriterData) {
			WriterData writerData = (WriterData) data;
			RTPSReader r = participant.getReaderForTopic(writerData.getTopicName());
			if (r != null) {
				GUID_t key = writerData.getKey();
				r.getHistoryCache(key); // Creates a history cache for this 
				if (false) { //key.entityId.isUserDefinedEntity()) { // Send AckNack to discovered writer
					//AckNack an = r.createAckNack(key, 1, 1); 
					//r.sendMessage(m, targetPrefix)
					Heartbeat hb = new Heartbeat(r.getGuid().entityId, key.entityId, 1, 1, 1);
					hb.finalFlag(false);
					r.onHeartbeat(key.prefix, hb);
				}
			}

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

	/**
	 * Handle builtin endpoints for discovered participant.
	 * If participant has a builtin reader for publications or subscriptions,
	 * send history cache to them.
	 * 
	 * @param builtinEndpoints
	 */
	private void handleBuiltinEnpointSet(int builtinEndpoints, Locator_t locator) {
		BuiltinEndpointSet eps = new BuiltinEndpointSet(builtinEndpoints);
		if (eps.hasPublicationDetector()) {
			//RTPSWriter pw = participant.getWriterForTopic("DCPSPublication");
			RTPSWriter pw = participant.getWriter(EntityId_t.SEDP_BUILTIN_PUBLICATIONS_WRITER);
			pw.sendHistoryCache(locator, EntityId_t.SEDP_BUILTIN_PUBLICATIONS_READER);
		}
		if (eps.hasSubscriptionDetector()) {
			//RTPSWriter pw = participant.getWriterForTopic("DCPSSubscription");
			RTPSWriter pw = participant.getWriter(EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER);
			pw.sendHistoryCache(locator, EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_READER);
		}
	}
}
