package alt.rtps.structure;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.discovery.DiscoveredData;
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

public class RTPSReader extends Reader {
	private static final Logger log = LoggerFactory.getLogger(RTPSReader.class);

	private List<DataListener> listeners = new LinkedList<DataListener>();
	private int ackNackCount = 0;
	private Marshaller marshaller;
	
	private final EntityId_t matchedEntity;

	public RTPSReader(GuidPrefix_t prefix, EntityId_t entityId, String topicName, Marshaller marshaller) {
		super(prefix, entityId, topicName);
		
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
	
	@Override
	public void onHeartbeat(GuidPrefix_t senderGuidPrefix, Heartbeat hb) {
		log.debug("{}", hb);
		sendAckNack(senderGuidPrefix, hb.getFirstSequenceNumber().getAsLong(), 
				hb.getLastSequenceNumber().getAsLong(), hb.finalFlag());
	}

	@Override
	public void onData(GuidPrefix_t prefix, Data data, Time_t timestamp) {
		
		Object obj = marshaller.unmarshall(data.getSerializedPayloadInputStream());
		
		if (obj instanceof DiscoveredData) {
			GUID_t writerGuid = new GUID_t(prefix, data.getWriterId()); // TODO: Do we need this info on discovered data
			((DiscoveredData) obj).setWriterGuid(writerGuid); 
		}
		
		HistoryCache hc = getHistoryCache(prefix);
		boolean dataAdded = hc.createChange(obj, data.getWriterSequenceNumber().getAsLong());
		
		if (dataAdded) {
			log.debug("Got " + obj.getClass().getSimpleName() + " for " + getGuid().entityId + ": " + data.getWriterSequenceNumber() + ", " + obj);
			for (DataListener dl : listeners) {
				dl.onData(obj, timestamp);
			}
		}
	}



	private void sendAckNack(GuidPrefix_t writerPrefix, long firstSeqNum, long lastSeqNum, boolean finalFlag) {
		Message m = new Message(getGuid().prefix);
		AckNack an = createAckNack(writerPrefix, firstSeqNum, lastSeqNum);
		m.addSubMessage(an);

		sendMessage(m, writerPrefix);
	}

	private AckNack createAckNack(GuidPrefix_t writerPrefix, long seqNumFirst, long seqNumLast) {
		// This is a simple AckNack, that can be optimized if store
		// out-of-order data samples in a separate cache.
		
		HistoryCache hc = getHistoryCache(writerPrefix);
		seqNumFirst = hc.getSeqNumMax(); // Positively ACK all that we have..
		int[] bitmaps = new int[] {-1}; // Negatively ACK rest

		SequenceNumberSet snSet = new SequenceNumberSet(seqNumFirst+1, bitmaps);
		
		AckNack an = new AckNack(getGuid().entityId, matchedEntity, snSet, ackNackCount++);

		return an;
	}
}
