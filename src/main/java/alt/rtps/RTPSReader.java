package alt.rtps;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.builtin.DiscoveredData;
import alt.rtps.builtin.WriterData;
import alt.rtps.message.AckNack;
import alt.rtps.message.Data;
import alt.rtps.message.Heartbeat;
import alt.rtps.message.Message;
import alt.rtps.transport.Marshaller;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.GUID_t;
import alt.rtps.types.GuidPrefix_t;
import alt.rtps.types.SequenceNumberSet;
import alt.rtps.types.Time_t;

public class RTPSReader extends Endpoint {
	private static final Logger log = LoggerFactory.getLogger(RTPSReader.class);

	private HashSet<WriterData> matchedWriters = new HashSet<>();
	private HashMap<GUID_t, HistoryCache> reader_caches = new HashMap<>();
	
	private HistoryCache reader_cache;
	
	private List<DataListener> listeners = new LinkedList<DataListener>();
	private int ackNackCount = 0;
	private Marshaller marshaller;

	private EntityId_t matchedEntity;

	public RTPSReader(GuidPrefix_t prefix, EntityId_t entityId, String topicName, Marshaller marshaller) {
		super(prefix, entityId, topicName);
		//this.reader_cache = new HistoryCache(new GUID_t(prefix, entityId));
		
		this.marshaller = marshaller;

		if (entityId.equals(EntityId_t.SPDP_BUILTIN_PARTICIPANT_READER)) {
			matchedEntity = EntityId_t.SPDP_BUILTIN_PARTICIPANT_WRITER;
		}
		else if (entityId.equals(EntityId_t.SEDP_BUILTIN_PUBLICATIONS_READER)) {
			matchedEntity = EntityId_t.SEDP_BUILTIN_PUBLICATIONS_WRITER;
		}
		else if (entityId.equals(EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_READER)) {
			matchedEntity = EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER;
		}
		else if (entityId.equals(EntityId_t.SEDP_BUILTIN_TOPIC_READER)) {
			matchedEntity = EntityId_t.SEDP_BUILTIN_TOPIC_WRITER;
		}
		else {
			log.warn("Setting matched entity for {}:{} to UNKNOWN_ENTITY", prefix, entityId);
			matchedEntity = EntityId_t.UNKNOWN_ENTITY;
		}
	}


	public void addListener(DataListener listener) {
		listeners.add(listener);
	}


	public void onData(GuidPrefix_t prefix, Data data, Time_t timestamp) throws IOException {

		Object obj = marshaller.unmarshall(data.getDataEncapsulation());
		GUID_t writerGuid = new GUID_t(prefix, data.getWriterId()); 

		if (obj instanceof DiscoveredData) {
			((DiscoveredData) obj).setWriterGuid(writerGuid); 
		}

		HistoryCache hc = getHistoryCache(writerGuid);
		boolean dataAdded = hc.createChange(obj, data.getWriterSequenceNumber().getAsLong());
		
		if (dataAdded) {
			log.debug("[{}] Got {}, {}: {}", getGuid().entityId, 
					obj.getClass().getSimpleName(), data.getWriterSequenceNumber(), obj);

			for (DataListener dl : listeners) {
				dl.onData(obj, timestamp);
			}
		}
		else {
			log.warn("[{}] Data was not added to cache: {}, {}", getGuid().entityId, data.getWriterSequenceNumber(), obj);
		}
	}


	public void onHeartbeat(GuidPrefix_t senderGuidPrefix, Heartbeat hb) {
		log.debug("[{}] Got {}", getGuid().entityId, hb); 
		if (!hb.finalFlag()) { // if the FinalFlag is not set, then the Reader must send an AckNack
			Message m = new Message(getGuid().prefix);
			//AckNack an = createAckNack(new GUID_t(senderGuidPrefix, hb.getWriterId()), hb.getFirstSequenceNumber().getAsLong(), hb.getLastSequenceNumber().getAsLong());
			AckNack an = createAckNack(new GUID_t(senderGuidPrefix, hb.getWriterId()));
			m.addSubMessage(an);
			log.debug("[{}] Sending {}", getGuid().entityId, an);
			sendMessage(m, senderGuidPrefix);
		}
	}


	private AckNack createAckNack(GUID_t writerGuid) {
		// This is a simple AckNack, that can be optimized if store
		// out-of-order data samples in a separate cache.

		HistoryCache hc = getHistoryCache(writerGuid);
		long seqNumFirst = hc.getSeqNumMax(); // Positively ACK all that we have..
		int[] bitmaps = new int[] {-1}; // Negatively ACK rest

		SequenceNumberSet snSet = new SequenceNumberSet(seqNumFirst+1, bitmaps);

		AckNack an = new AckNack(getGuid().entityId, matchedEntity, snSet, ackNackCount++);

		return an;
	}
	
	private HistoryCache getHistoryCache(GUID_t writerGuid) {
		HistoryCache hc = reader_caches.get(writerGuid);;
		if (hc == null) {
			hc = new HistoryCache(writerGuid);
			reader_caches.put(writerGuid, hc);
		}
		
		return hc;
	}

	/**
	 * Get the BuiltinEndpointSet ID of this RTPSReader.
	 * 
	 * @return 0, if this RTPSReader is not builtin endpoint
	 */
	int endpointSetId() {
		return getGuid().entityId.getEndpointSetId();
	}

	public void close() {
		reader_caches.values();
		for (HistoryCache hc : reader_caches.values()) {
			hc.getChanges().clear();
		}
	}

	void addMatchedWriter(WriterData writerData) {
		matchedWriters.add(writerData);
	}
}
