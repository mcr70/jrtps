package alt.rtps;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.builtin.DiscoveredData;
import alt.rtps.builtin.ParticipantData;
import alt.rtps.builtin.ReaderData;
import alt.rtps.builtin.TopicData;
import alt.rtps.builtin.WriterData;
import alt.rtps.message.parameter.BuiltinEndpointSet;
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
		if (data instanceof DiscoveredData) {
			DiscoveredData dd = (DiscoveredData)data;
			GUID_t key = dd.getKey();
			ParticipantData pd = discoveredParticipants.get(key.prefix);
			if (pd != null) {
				if (key.entityId.isBuiltinEntity()) {
					dd.setLocator(pd.getMetatrafficUnicastLocator());
				}
				else {
					dd.setLocator(pd.getUnicastLocator());
				}
			}
		}
		
		if (data instanceof ParticipantData) {	
			handleParticipantData((ParticipantData) data);
		}
		else if (data instanceof WriterData) {
			handleWriterData((WriterData) data);
		}
		else if (data instanceof ReaderData) {
			handleReaderData((ReaderData) data);
		}
		else if (data instanceof TopicData) {
			handleTopicData((TopicData) data);
		}
	}



	private void handleParticipantData(ParticipantData pd) {
		log.trace("Considering Participant {}", pd.getGuid());

		ParticipantData d = discoveredParticipants.get(pd.getGuidPrefix());
		if (d == null && pd.getGuidPrefix() != null) {
			if (pd.getGuidPrefix().equals(participant.guid.prefix)) {
				log.trace("Ignoring self");
			}
			else {
				log.debug("A new Participant detected: {}", pd); //.getGuidPrefix() + ", " + pd.getAllLocators());
				discoveredParticipants.put(pd.getGuidPrefix(), pd);

				// First, make sure remote participant knows about us.
				RTPSWriter pw = participant.getWriter(EntityId_t.SPDP_BUILTIN_PARTICIPANT_WRITER);
				pw.sendHistoryCache(pd.getMetatrafficUnicastLocator(), EntityId_t.SPDP_BUILTIN_PARTICIPANT_READER);

				// Then, announce our builtin endpoints
				handleBuiltinEnpointSet(pd.getBuiltinEndpoints(), pd.getMetatrafficUnicastLocator());
			}
		}
	}

	private void handleWriterData(WriterData writerData) {
		discoveredWriters.put(writerData.getWriterGuid(), writerData);
		
		RTPSReader r = participant.getReaderForTopic(writerData.getTopicName());
		if (r != null) {
			r.addMatchedWriter(writerData);
		}
	}

	/**
	 * Handler discovered ReaderData. If ReaderData represents an user defined
	 * Reader, and this participant has a Writer for same topic, send writers history
	 * cache to reader.
	 * 
	 * @param readerData
	 */
	private void handleReaderData(ReaderData readerData) {
		discoveredReaders.put(readerData.getReaderGuid(), readerData);
		GUID_t key = readerData.getKey();

		RTPSWriter writer = participant.getWriterForTopic(readerData.getTopicName());
		if (writer != null) {
			writer.addMatchedReader(readerData);
		}
		
		// builtin entities are handled with SEDP in ParticipantData reception
		if (key.entityId.isUserDefinedEntity()) {  
			
			if (writer != null) {
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

	private void handleTopicData(TopicData data) {
		TopicData topicData = (TopicData) data;
		discoveredTopics.put(topicData.getKey(), topicData);
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
