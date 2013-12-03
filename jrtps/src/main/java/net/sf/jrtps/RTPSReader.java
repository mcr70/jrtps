package net.sf.jrtps;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.builtin.WriterData;
import net.sf.jrtps.message.AckNack;
import net.sf.jrtps.message.Data;
import net.sf.jrtps.message.Heartbeat;
import net.sf.jrtps.message.Message;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.types.EntityId_t;
import net.sf.jrtps.types.GUID_t;
import net.sf.jrtps.types.GuidPrefix_t;
import net.sf.jrtps.types.SequenceNumberSet;
import net.sf.jrtps.types.Time_t;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RTPSReader implements RTPS Reader endpoint functionality.
 * RTPSReader does not store any data received. It only keeps track of data
 * entries sent by writers and propagates received data to SampleListeners registered.
 * 
 * @author mcr70
 * @see SampleListener
 */
public class RTPSReader<T> extends Endpoint {
	private static final Logger log = LoggerFactory.getLogger(RTPSReader.class);

	//private HashSet<WriterData> matchedWriters = new HashSet<>();
	private final HashMap<GUID_t, WriterProxy> writerProxies = new HashMap<>();
	private final List<SampleListener<T>> sampleListeners = new LinkedList<SampleListener<T>>();
	private final Marshaller<?> marshaller;
	private final List<Sample<T>> pendingSamples = new LinkedList<>();
	private final int heartbeatResponseDelay;
	
	private int ackNackCount = 0;
	
	RTPSReader(RTPSParticipant participant, EntityId_t entityId, String topicName, Marshaller<?> marshaller, 
			QualityOfService qos, Configuration configuration) {
		super(participant, entityId, topicName, qos, configuration);

		this.marshaller = marshaller;
		this.heartbeatResponseDelay = configuration.getHeartbeatResponseDelay();
	}


	/**
	 * Adds a SampleListener to this RTPSReader.
	 * 
	 * @param listener SampleListener to add.
	 */
	public void addListener(SampleListener<T> listener) {
		log.debug("[{}] Adding SampleListener {} for topic {}", getGuid().entityId, listener, getTopicName());
		sampleListeners.add(listener);
	}

	/**
	 * Removes a SampleListener from this RTPSReader.
	 * 
	 * @param listener SampleListener to remove
	 */
	public void removeListener(SampleListener<T> listener) {
		log.debug("[{}] Removing SampleListener {} from topic {}", getGuid().entityId, listener, getTopicName());
		sampleListeners.remove(listener);
	}

	/**
	 * Handle incoming Data message.
	 * 
	 * @param sourcePrefix GuidPrefix of the remote participant sending Data message 
	 * @param data
	 * @param timestamp
	 * @throws IOException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void createSample(GuidPrefix_t sourcePrefix, Data data, Time_t timestamp) throws IOException {

		GUID_t writerGuid = new GUID_t(sourcePrefix, data.getWriterId()); 

		WriterProxy wp = getWriterProxy(writerGuid);

		if (wp.acceptData(data.getWriterSequenceNumber())) {
			Object obj = marshaller.unmarshall(data.getDataEncapsulation());
			log.debug("[{}] Got Data: {}, {}", getGuid().entityId, 
					obj.getClass().getSimpleName(), data.getWriterSequenceNumber());

			synchronized (pendingSamples) {
				pendingSamples.add(new Sample(obj, timestamp, data.getStatusInfo()));	
			}
		}
		else {
			log.debug("[{}] Data was rejected: Data seq-num={}, proxy seq-num={}", getGuid().entityId, 
					data.getWriterSequenceNumber(), wp.getSeqNumMax());
		}
	}

	/**
	 * Handle incoming HeartBeat message.
	 * 
	 * @param senderGuidPrefix
	 * @param hb
	 */
	void onHeartbeat(GuidPrefix_t senderGuidPrefix, Heartbeat hb) {
		log.debug("[{}] Got Heartbeat: {}-{}", getGuid().entityId, hb.getFirstSequenceNumber(), hb.getLastSequenceNumber());

		WriterProxy wp = getWriterProxy(new GUID_t(senderGuidPrefix, hb.getWriterId()));
		if (hb.livelinessFlag()) {
			// TODO: implement liveliness
		}

		if (isReliable()) { // Only reliable readers respond to heartbeat
			boolean doSend = false;
			if (!hb.finalFlag()) { // if the FinalFlag is not set, then the Reader must send an AckNack
				doSend = true;
			}
			else {
				if (wp.acceptHeartbeat(hb.getLastSequenceNumber())) {
					doSend = true;
				}
				else {
					log.trace("[{}] Will no send AckNack, since my seq-num is {} and Heartbeat seq-num is {}", 
							getGuid().entityId, wp.getSeqNumMax(), hb.getLastSequenceNumber());
				}
			}

			if (doSend) {
				Message m = new Message(getGuid().prefix);
				//AckNack an = createAckNack(new GUID_t(senderGuidPrefix, hb.getWriterId()), hb.getFirstSequenceNumber().getAsLong(), hb.getLastSequenceNumber().getAsLong());
				AckNack an = createAckNack(new GUID_t(senderGuidPrefix, hb.getWriterId()));
				m.addSubMessage(an);
				
				log.debug("[{}] Wait for heartbeat response delay: {} ms", getGuid().entityId, heartbeatResponseDelay);
				getParticipant().waitFor(heartbeatResponseDelay);

				log.debug("[{}] Sending AckNack: {}", getGuid().entityId, an.getReaderSNState());
				sendMessage(m, senderGuidPrefix);
			}
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
		WriterProxy wp = writerProxies.get(writerGuid);
		if (wp == null) {
			// TODO: Ideally, we should not need to do this. For now, builtin entities need this behaviour:
			//       Remote entities are assumed alive even though corresponding discovery data has not been
			//       received yet. I.e. during discovery, BuiltinEnpointSet is received with ParticipantData.
			log.debug("[{}] Creating proxy for {}", getGuid().entityId, writerGuid); 
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
		//matchedWriters.add(writerData);
		writerProxies.put(writerData.getKey(), new WriterProxy(writerData));

		log.debug("[{}] Adding matchedWriter {}", getGuid().entityId, writerData);
	}
	void removeMatchedWriter(WriterData writerData) {
		log.debug("[{}] Removing matchedWriter {}", getGuid().entityId, writerData);
		
		writerProxies.remove(writerData.getKey());
		//matchedWriters.remove(writerData);
	}


	/**
	 * Releases pending samples.
	 */
	void releasePendingSamples() {
		// TODO: pending samples need to be handled differently.
		//       Maybe have a flag that tells if there is more pending samples
		//       at the end of the method.
		LinkedList<Sample<T>> ll = new LinkedList<>();

		synchronized(pendingSamples) {
			ll.addAll(pendingSamples);
			pendingSamples.clear();
		}

		log.debug("[{}] Got {} samples", getGuid().entityId, ll.size());
		
		if (ll.size() > 0) {
			for (SampleListener<T> sl: sampleListeners) {
				sl.onSamples(ll);
			}
		}
	}

	private boolean isReliable() {
		QosReliability reliability = (QosReliability) getQualityOfService().getPolicy(QosReliability.class);
		return reliability.getKind() == QosReliability.Kind.RELIABLE;
	}
}
