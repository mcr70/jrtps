package net.sf.jrtps;

import java.io.IOException;
import java.util.Collection;
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
 * RTPSReader does not store any data received. It only keeps track of data entries sent by writers and 
 * propagates received data to SampleListeners registered.<p>
 * 
 * RTPSReader will not communicate with unknown writers. It is the responsibility of 
 * DDS layer to provide matched readers when necessary. Likewise, DDS layer should remove 
 * matched writer, when it detects that it is not available anymore. 
 * 
 * @author mcr70
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
		log.debug("[{}] Adding SampleListener {} for topic {}", getGuid().getEntityId(), listener, getTopicName());
		sampleListeners.add(listener);
	}

	/**
	 * Removes a SampleListener from this RTPSReader.
	 * 
	 * @param listener SampleListener to remove
	 */
	public void removeListener(SampleListener<T> listener) {
		log.debug("[{}] Removing SampleListener {} from topic {}", getGuid().getEntityId(), listener, getTopicName());
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
		return getGuid().getEntityId().getEndpointSetId();
	}

	/**
	 * Closes this RTPSReader.
	 */
	public void close() {
		// TODO: No use for this
	}

	/**
	 * Adds a matched writer for this RTPSReader.
	 * 
	 * @param writerData
	 * @return WriterProxy
	 */
	public WriterProxy addMatchedWriter(WriterData writerData) {
		WriterProxy wp = new WriterProxy(writerData);
		writerProxies.put(writerData.getKey(), wp);

		log.info("[{}] Added matchedWriter {}", getGuid().getEntityId(), writerData);
		return wp;
	}

	/**
	 * Removes a matched writer from this RTPSReader.
	 * @param writerData writer to remove. If corresponding writer does not exists, this method silently returns
	 */
	public void removeMatchedWriter(WriterData writerData) {
		writerProxies.remove(writerData.getKey());

		log.info("[{}] Removed matchedWriter {}", getGuid().getEntityId(), writerData);
	}

	/**
	 * Gets all the matched writers of this RTPSReader.
	 * @return 
	 */
	public Collection<WriterProxy> getMatchedWriters() {
		return writerProxies.values();
	}
	
	/**
	 * Gets the matched writers owned by given remote participant.
	 * 
	 * @param prefix GuidPrefix of the remote participant
	 * @return a Collection of matched writers
	 */
	public Collection<WriterProxy> getMatchedWriters(GuidPrefix prefix) {
		List<WriterProxy> proxies = new LinkedList<>();
		for (Guid guid : writerProxies.keySet()) {
			if (guid.getPrefix().equals(prefix)) {
				proxies.add(writerProxies.get(guid));
			}
		}
		return proxies;
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
				log.trace("[{}] Got Data: {}, {}", getGuid().getEntityId(), 
						obj.getClass().getSimpleName(), data.getWriterSequenceNumber());

				synchronized (pendingSamples) {
					pendingSamples.add(new Sample(obj, timestamp, data.getStatusInfo()));	
				}
			}
			else {
				log.debug("[{}] Data was rejected: Data seq-num={}, proxy seq-num={}", getGuid().getEntityId(), 
						data.getWriterSequenceNumber(), wp.getSeqNumMax());
			}
		}
		else {
			log.warn("[{}] Discarding Data from unknown writer {}", getGuid().getEntityId(), data.getWriterId());
		}
	}

	/**
	 * Handle incoming HeartBeat message.
	 * 
	 * @param senderGuidPrefix
	 * @param hb
	 */
	void onHeartbeat(GuidPrefix senderGuidPrefix, Heartbeat hb) {
		log.debug("[{}] Got Heartbeat: {}-{}", getGuid().getEntityId(), hb.getFirstSequenceNumber(), hb.getLastSequenceNumber());

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
								getGuid().getEntityId(), wp.getSeqNumMax(), hb.getLastSequenceNumber());
					}
				}

				if (doSend) {
					Message m = new Message(getGuid().getPrefix());
					AckNack an = createAckNack(wp);
					m.addSubMessage(an);

					log.debug("[{}] Wait for heartbeat response delay: {} ms", getGuid().getEntityId(), heartbeatResponseDelay);
					getParticipant().waitFor(heartbeatResponseDelay);

					log.debug("[{}] Sending AckNack: {}", getGuid().getEntityId(), an.getReaderSNState());
					sendMessage(m, senderGuidPrefix);
				}
			}
		}
		else {
			log.warn("[{}] Discarding Heartbeat from unknown writer {}", getGuid().getEntityId(), hb.getWriterId());
		}
	}


	private AckNack createAckNack(WriterProxy wp) {
		// This is a simple AckNack, that can be optimized if store
		// out-of-order data samples in a separate cache.

		long seqNumFirst = wp.getSeqNumMax(); // Positively ACK all that we have..
		int[] bitmaps = new int[] {-1}; // Negatively ACK rest
		SequenceNumberSet snSet = new SequenceNumberSet(seqNumFirst+1, bitmaps);

		AckNack an = new AckNack(getGuid().getEntityId(), wp.getGuid().getEntityId(), snSet, ackNackCount++);

		return an;
	}

	private WriterProxy getWriterProxy(Guid writerGuid) {
		WriterProxy wp = writerProxies.get(writerGuid);
		if (wp == null) {
			if (writerGuid.getEntityId().isBuiltinEntity()) {
				// TODO: Ideally, we should not need to do this. For now, builtin entities need this behaviour:
				//       Remote entities are assumed alive even though corresponding discovery data has not been
				//       received yet. I.e. during discovery, BuiltinEnpointSet is received with ParticipantData.
				log.debug("[{}] Creating proxy for {}", getGuid().getEntityId(), writerGuid); 
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

		log.debug("[{}] Got {} samples", getGuid().getEntityId(), ll.size());

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
