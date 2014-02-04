package net.sf.jrtps;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.message.AckNack;
import net.sf.jrtps.message.Data;
import net.sf.jrtps.message.Gap;
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

	private final Map<Guid, WriterProxy> writerProxies = new ConcurrentHashMap<>();
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
		log.trace("[{}] Adding SampleListener {} for topic {}", getGuid().getEntityId(), listener, getTopicName());
		sampleListeners.add(listener);
	}

	/**
	 * Removes a SampleListener from this RTPSReader.
	 * 
	 * @param listener SampleListener to remove
	 */
	public void removeListener(SampleListener<T> listener) {
		log.trace("[{}] Removing SampleListener {} from topic {}", getGuid().getEntityId(), listener, getTopicName());
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
	 * Adds a matched writer for this RTPSReader.
	 * 
	 * @param writerData
	 * @return WriterProxy
	 */
	public WriterProxy addMatchedWriter(PublicationData writerData) {
		WriterProxy wp = new WriterProxy(writerData);
		writerProxies.put(writerData.getKey(), wp);	

		log.debug("[{}] Added matchedWriter {}", getGuid().getEntityId(), writerData);

		return wp;
	}

	/**
	 * Removes all the matched writers that have a given GuidPrefix
	 * @param prefix
	 */
	public void removeMatchedWriters(GuidPrefix prefix) {
		for (WriterProxy wp : writerProxies.values()) {
			if (prefix.equals(wp.getGuid().getPrefix())) {
				removeMatchedWriter(wp.getPublicationData());
			}
		}
	}

	/**
	 * Removes a matched writer from this RTPSReader.
	 * @param writerData writer to remove. If corresponding writer does not exists, this method silently returns
	 */
	public void removeMatchedWriter(PublicationData writerData) {
		writerProxies.remove(writerData.getKey());	

		log.debug("[{}] Removed matchedWriter {}", getGuid().getEntityId(), writerData.getKey());
	}

	/**
	 * Gets all the matched writers of this RTPSReader.
	 * @return a Collection of matched writers 
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
	 * Handle incoming Data message. Data is unmarshalled and added to pending samples.
	 * Once RTPSMessageHandler has finished with the whole RTPSMessage, it will call 
	 * releasePendingSamples of each RTPSReader that has received some Data messages.
	 * 
	 * @param sourcePrefix GuidPrefix of the remote participant sending Data message 
	 * @param data
	 * @param timestamp
	 * @throws IOException
	 * @see #releasePendingSamples()
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void createSample(GuidPrefix sourcePrefix, Data data, Time timestamp) throws IOException {

		Guid writerGuid = new Guid(sourcePrefix, data.getWriterId()); 

		WriterProxy wp = getWriterProxy(writerGuid);
		if (wp != null) {
			if (wp.acceptData(data.getWriterSequenceNumber())) {
				Object obj = marshaller.unmarshall(data.getDataEncapsulation());
				log.debug("[{}] Got Data: {}", getGuid().getEntityId(), data.getWriterSequenceNumber());

				pendingSamples.add(new Sample(obj, timestamp, data.getStatusInfo()));	
			}
			else {
				log.debug("[{}] Data was rejected: Data seq-num={}, proxy seq-num={}", getGuid().getEntityId(), 
						data.getWriterSequenceNumber(), wp.getGreatestDataSeqNum());
			}
		}
		else {
			log.warn("[{}] Discarding Data from unknown writer {}, {}", getGuid().getEntityId(), sourcePrefix, data.getWriterId());
		}
	}

	/**
	 * Handle incoming HeartBeat message.
	 * 
	 * @param senderGuidPrefix
	 * @param hb
	 */
	void onHeartbeat(GuidPrefix senderGuidPrefix, Heartbeat hb) {
		log.debug("[{}] Got Heartbeat: #{} {}-{}, F:{}, L:{} from {}", getGuid().getEntityId(), 
				hb.getCount(), hb.getFirstSequenceNumber(), hb.getLastSequenceNumber(), 
				hb.finalFlag(), hb.livelinessFlag(), senderGuidPrefix);

		WriterProxy wp = getWriterProxy(new Guid(senderGuidPrefix, hb.getWriterId()));
		if (wp != null) {
			if (wp.heartbeatReceived(hb)) {
				if (hb.livelinessFlag()) {
					wp.assertLiveliness();
				}

				if (isReliable()) { // Only reliable readers respond to heartbeat
					boolean doSend = false;
					if (!hb.finalFlag()) { // if the FinalFlag is not set, then the Reader must send an AckNack
						doSend = true;
					}
					else {
						if (wp.getGreatestDataSeqNum() < hb.getLastSequenceNumber()) {
							doSend = true;
						}
						else {
							log.trace("[{}] Will no send AckNack, since my seq-num is {} and Heartbeat seq-num is {}", 
									getGuid().getEntityId(), wp.getGreatestDataSeqNum(), hb.getLastSequenceNumber());
						}
					}

					if (doSend) {
						sendAckNack(wp);
					}
				}
			}
			else {
				log.debug("[{}] Ignoring Heartbeat whose count is {}, since proxys count is {}", getGuid().getEntityId(), hb.getCount(), wp.getLatestHeartbeatCount());
			}
		}
		else {
			log.warn("[{}] Discarding Heartbeat from unknown writer {}, {}", getGuid().getEntityId(), senderGuidPrefix, hb.getWriterId());
		}
	}

	private void sendAckNack(WriterProxy wp) {
		log.trace("[{}] Wait for heartbeat response delay: {} ms", getGuid().getEntityId(), heartbeatResponseDelay);
		getParticipant().waitFor(heartbeatResponseDelay);

		Message m = new Message(getGuid().getPrefix());

		AckNack an = createAckNack(wp);   // If all the data is already received, set finalFlag to true, 
		an.finalFlag(wp.isAllReceived()); // otherwise false(response required)

		m.addSubMessage(an);

		GuidPrefix targetPrefix = wp.getGuid().getPrefix(); 

		log.debug("[{}] Sending AckNack: #{} {}, F:{} to {}", getGuid().getEntityId(), 
				an.getCount(), an.getReaderSNState(), an.finalFlag(), targetPrefix);

		//sendMessage(m, wp);
		sendMessage(m, targetPrefix);
	}

	private AckNack createAckNack(WriterProxy wp) {
		// This is a simple AckNack, that can be optimized if store
		// out-of-order data samples in a separate cache.

		long seqNumFirst = wp.getGreatestDataSeqNum(); // Positively ACK all that we have..
		int[] bitmaps = new int[] {-1}; // Negatively ACK rest
		SequenceNumberSet snSet = new SequenceNumberSet(seqNumFirst+1, bitmaps);

		AckNack an = new AckNack(getGuid().getEntityId(), wp.getGuid().getEntityId(), snSet, ++ackNackCount);

		return an;
	}

	private WriterProxy getWriterProxy(Guid writerGuid) {
		WriterProxy wp = writerProxies.get(writerGuid);

		if (wp == null && EntityId.SPDP_BUILTIN_PARTICIPANT_WRITER.equals(writerGuid.getEntityId())) {
			log.debug("[{}] Creating proxy for SPDP writer {}", getGuid().getEntityId(), writerGuid); 
			PublicationData pd = new PublicationData(ParticipantData.BUILTIN_TOPIC_NAME, ParticipantData.class.getName(), 
					writerGuid, new SPDPQualityOfService());
			wp = new WriterProxy(pd);

			writerProxies.put(writerGuid, wp);	
		}

		return wp;
	}



	/**
	 * Releases pending samples. Data is unmarshalled and added to pending samples.
	 * Once RTPSMessageHandler has finished with the whole RTPSMessage, it will call 
	 * releasePendingSamples of each RTPSReader that has received some Data messages.
	 * 
	 * @see #createSample(GuidPrefix, Data, Time)
	 */
	void releasePendingSamples() {
		LinkedList<Sample<T>> ll = new LinkedList<>();

		ll.addAll(pendingSamples);
		pendingSamples.clear();

		log.trace("[{}] Got {} samples", getGuid().getEntityId(), ll.size());

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

	/**
	 * Handles Gap submessage by updating WriterProxy.
	 * @param sourceGuidPrefix
	 * @param gap
	 */
	void handleGap(GuidPrefix sourcePrefix, Gap gap) {
		Guid writerGuid = new Guid(sourcePrefix, gap.getWriterId()); 

		WriterProxy wp = getWriterProxy(writerGuid);
		if (wp != null) {
			wp.applyGap(gap);
		}
	}


	public boolean isMatchedWith(PublicationData writerData) {
		return writerProxies.get(writerData.getKey()) != null;
	}
}
