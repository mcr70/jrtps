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
import alt.rtps.types.EntityId_t;
import alt.rtps.types.GUID_t;
import alt.rtps.types.GuidPrefix_t;
import alt.rtps.types.SequenceNumberSet;
import alt.rtps.types.Time_t;

/**
 * RTPSReader implements RTPS Reader endpoint functionality.
 * RTPSReader does not store any data received. It only keeps track of data
 * entries sent by writers and propagates received data to DataListeners registered.
 * 
 * @author mcr70
 * @see DataListener
 */
public class RTPSReader extends Endpoint {
	private static final Logger log = LoggerFactory.getLogger(RTPSReader.class);

	private HashSet<WriterData> matchedWriters = new HashSet<>();
	private HashMap<GUID_t, WriterProxy> writerProxies = new HashMap<>();
		
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

	/**
	 * Handle incoming Data message.
	 * 
	 * @param sourcePrefix GuidPrefix of the remote participant sending Data message 
	 * @param data
	 * @param timestamp
	 * @throws IOException
	 */
	public void onData(GuidPrefix_t sourcePrefix, Data data, Time_t timestamp) throws IOException {

		GUID_t writerGuid = new GUID_t(sourcePrefix, data.getWriterId()); 

		WriterProxy wp = getWriterProxy(writerGuid);
		
		if (wp.acceptData(data.getWriterSequenceNumber())) {
			Object obj = marshaller.unmarshall(data.getDataEncapsulation());
			log.debug("[{}] Got Data: {}, {}", getGuid().entityId, 
					obj.getClass().getSimpleName(), data.getWriterSequenceNumber());

			for (DataListener dl : listeners) {
				dl.onData(obj, timestamp);
			}
		}
		else {
			log.warn("[{}] Data was rejected: Data seq-num={}, proxy seq-num={}", getGuid().entityId, 
					data.getWriterSequenceNumber(), wp.getSeqNumMax());
		}
	}

	/**
	 * Handle incoming HeartBeat message.
	 * 
	 * @param senderGuidPrefix
	 * @param hb
	 */
	public void onHeartbeat(GuidPrefix_t senderGuidPrefix, Heartbeat hb) {
		log.debug("[{}] Got Heartbeat: {}-{}", getGuid().entityId, hb.getFirstSequenceNumber(), hb.getLastSequenceNumber());
		boolean doSend = false;
		if (!hb.finalFlag()) { // if the FinalFlag is not set, then the Reader must send an AckNack
			doSend = true;
		}
		else {
			WriterProxy wp = getWriterProxy(new GUID_t(senderGuidPrefix, hb.getWriterId()));
			if (wp.acceptHeartbeat(hb.getLastSequenceNumber())) {
				doSend = true;
			}
			else {
				log.debug("Will no send AckNack, since my seq-num is {} and Heartbeat seq-num is {}", wp.getSeqNumMax(), hb.getLastSequenceNumber());
			}
		}

		if (doSend) {
			Message m = new Message(getGuid().prefix);
			//AckNack an = createAckNack(new GUID_t(senderGuidPrefix, hb.getWriterId()), hb.getFirstSequenceNumber().getAsLong(), hb.getLastSequenceNumber().getAsLong());
			AckNack an = createAckNack(new GUID_t(senderGuidPrefix, hb.getWriterId()));
			m.addSubMessage(an);
			log.debug("[{}] Sending AckNack: {}", getGuid().entityId, an.getReaderSNState());
			sendMessage(m, senderGuidPrefix);
		}
	}


	private AckNack createAckNack(GUID_t writerGuid) {
		// This is a simple AckNack, that can be optimized if store
		// out-of-order data samples in a separate cache.

		WriterProxy wp = getWriterProxy(writerGuid);
		long seqNumFirst = wp.getSeqNumMax(); // Positively ACK all that we have..
		int[] bitmaps = new int[] {-1}; // Negatively ACK rest

		SequenceNumberSet snSet = new SequenceNumberSet(seqNumFirst+1, bitmaps);

		AckNack an = new AckNack(getGuid().entityId, writerGuid.entityId, snSet, ackNackCount++);

		return an;
	}
	
	private WriterProxy getWriterProxy(GUID_t writerGuid) {
		WriterProxy wp = writerProxies.get(writerGuid);;
		if (wp == null) {
			wp = new WriterProxy(writerGuid);
			writerProxies.put(writerGuid, wp);
		}
		
		return wp;
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
		// TODO: No use for this
	}

	void addMatchedWriter(WriterData writerData) {
		matchedWriters.add(writerData);
	}
}
