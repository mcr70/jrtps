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
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.types.SequenceNumberSet;
import net.sf.jrtps.types.Time;

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
	private final HashMap<Guid, WriterProxy> writerProxies = new HashMap<>();
	private final List<SampleListener<T>> sampleListeners = new LinkedList<SampleListener<T>>();
	private final Marshaller<?> marshaller;
	private final List<Sample<T>> pendingSamples = new LinkedList<>();
	private final int heartbeatResponseDelay;

	private int ackNackCount = 0;

	RTPSReader(RTPSParticipant participant, EntityId entityId, String topicName, Marshaller<?> marshaller, 
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
	 * Get the BuiltinEndpointSet ID of this RTPSReader. endpointSetId represents a bit in 
	 * BuiltinEndpointSet_t, found during discovery. Each bit represents an existence of a predefined 
	 * builtin entity.<p>
	 * See 8.5.4.3 Built-in Endpoints required by the Simple Endpoint Discovery Protocol and table 9.4 BuiltinEndpointSet_t.
	 * 
	 * @return 0, if this RTPSReader is not builtin endpoint
	 */
	public int endpointSetId() {
		return getGuid().entityId.getEndpointSetId();
	}

	/**
	 * Closes this RTPSReader.
	 */
	public void close() {
		// TODO: No use for this
	}

	public void addMatchedWriter(WriterData writerData) {
		writerProxies.put(writerData.getKey(), new WriterProxy(writerData));

		log.info("[{}] Adding matchedWriter {}", getGuid().entityId, writerData);
	}
	public void removeMatchedWriter(WriterData writerData) {
		log.info("[{}] Removing matchedWriter {}", getGuid().entityId, writerData);

		writerProxies.remove(writerData.getKey());
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
	void createSample(GuidPrefix sourcePrefix, Data data, Time timestamp) throws IOException {

		Guid writerGuid = new Guid(sourcePrefix, data.getWriterId()); 

		WriterProxy wp = getWriterProxy(writerGuid);
		if (wp != null) {
			if (wp.acceptData(data.getWriterSequenceNumber())) {
				Object obj = marshaller.unmarshall(data.getDataEncapsulation());
				log.trace("[{}] Got Data: {}, {}", getGuid().entityId, 
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
		else {
			log.warn("[{}] Discarding Data from unknown writer {}", getGuid().entityId, data.getWriterId());
		}
	}

	/**
	 * Handle incoming HeartBeat message.
	 * 
	 * @param senderGuidPrefix
	 * @param hb
	 */
	void onHeartbeat(GuidPrefix senderGuidPrefix, Heartbeat hb) {
		log.debug("[{}] Got Heartbeat: {}-{}", getGuid().entityId, hb.getFirstSequenceNumber(), hb.getLastSequenceNumber());

		WriterProxy wp = getWriterProxy(new Guid(senderGuidPrefix, hb.getWriterId()));
		if (wp != null) {
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
					AckNack an = createAckNack(wp);
					m.addSubMessage(an);

					log.debug("[{}] Wait for heartbeat response delay: {} ms", getGuid().entityId, heartbeatResponseDelay);
					getParticipant().waitFor(heartbeatResponseDelay);

					log.debug("[{}] Sending AckNack: {}", getGuid().entityId, an.getReaderSNState());
					sendMessage(m, senderGuidPrefix);
				}
			}
		}
		else {
			log.warn("[{}] Discarding Heartbeat from unknown writer {}", getGuid().entityId, hb.getWriterId());
		}
	}


	private AckNack createAckNack(WriterProxy wp) {
		// This is a simple AckNack, that can be optimized if store
		// out-of-order data samples in a separate cache.

		long seqNumFirst = wp.getSeqNumMax(); // Positively ACK all that we have..
		int[] bitmaps = new int[] {-1}; // Negatively ACK rest
		SequenceNumberSet snSet = new SequenceNumberSet(seqNumFirst+1, bitmaps);

		AckNack an = new AckNack(getGuid().entityId, wp.getGuid().entityId, snSet, ackNackCount++);

		return an;
	}

	private WriterProxy getWriterProxy(Guid writerGuid) {
		WriterProxy wp = writerProxies.get(writerGuid);
		if (wp == null) {
			if (writerGuid.entityId.isBuiltinEntity()) {
				// TODO: Ideally, we should not need to do this. For now, builtin entities need this behaviour:
				//       Remote entities are assumed alive even though corresponding discovery data has not been
				//       received yet. I.e. during discovery, BuiltinEnpointSet is received with ParticipantData.
				log.debug("[{}] Creating proxy for {}", getGuid().entityId, writerGuid); 
				wp = new WriterProxy(writerGuid);
				writerProxies.put(writerGuid, wp); 
			}
		}

		return wp;
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
