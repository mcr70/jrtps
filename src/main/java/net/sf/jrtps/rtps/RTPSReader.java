package net.sf.jrtps.rtps;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.Marshaller;
import net.sf.jrtps.QualityOfService;
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
import net.sf.jrtps.types.Locator;
import net.sf.jrtps.types.SequenceNumberSet;
import net.sf.jrtps.types.Time;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RTPSReader implements RTPS Reader endpoint functionality. RTPSReader does not
 * store any data received. It only keeps track of data entries sent by writers
 * and propagates received data to RTPSListeners registered.
 * <p>
 * 
 * RTPSReader will not communicate with unknown writers. It is the
 * responsibility of DDS layer to provide matched readers when necessary.
 * Likewise, DDS layer should remove matched writer, when it detects that it is
 * not available anymore.
 * 
 * @author mcr70
 */
public class RTPSReader<T> extends Endpoint {
	private static final Logger log = LoggerFactory.getLogger(RTPSReader.class);

	private final Map<Guid, WriterProxy> writerProxies = new ConcurrentHashMap<>();
	private final List<RTPSListener<T>> listeners = new LinkedList<RTPSListener<T>>();
	private final Marshaller<T> marshaller;
	private final ReaderCache<T> rCache;
	private final List<Sample<T>> pendingSamples = new LinkedList<>();
	private final int heartbeatResponseDelay;
	private final int heartbeatSuppressionDuration;

	private int ackNackCount = 0;

    

	RTPSReader(RTPSParticipant participant, EntityId entityId, String topicName, Marshaller<T> marshaller,
			ReaderCache<T> rCache, QualityOfService qos, Configuration configuration) {
		super(participant, entityId, topicName, qos, configuration);

		this.marshaller = marshaller;
        this.rCache = rCache;
		this.heartbeatResponseDelay = configuration.getHeartbeatResponseDelay();
		this.heartbeatSuppressionDuration = configuration.getHeartbeatSuppressionDuration();
	}

	/**
	 * Adds a RTPSListener to this RTPSReader.
	 * 
	 * @param listener RTPSListener to add.
	 */
	public void addListener(RTPSListener<T> listener) {
		log.trace("[{}] Adding RTPSListener {} for topic {}", getEntityId(), listener, getTopicName());
		listeners.add(listener);
	}

	/**
	 * Removes a RTPSListener from this RTPSReader.
	 * 
	 * @param listener RTPSListener to remove
	 */
	public void removeListener(RTPSListener<T> listener) {
		log.trace("[{}] Removing RTPSListener {} from topic {}", getEntityId(), listener, getTopicName());
		listeners.remove(listener);
	}

	/**
	 * Get the BuiltinEndpointSet ID of this RTPSReader. endpointSetId
	 * represents a bit in BuiltinEndpointSet_t, found during discovery. Each
	 * bit represents an existence of a predefined builtin entity.
	 * <p>
	 * See 8.5.4.3 Built-in Endpoints required by the Simple Endpoint Discovery
	 * Protocol and table 9.4 BuiltinEndpointSet_t.
	 * 
	 * @return 0, if this RTPSReader is not builtin endpoint
	 */
	public int endpointSetId() {
		return getEntityId().getEndpointSetId();
	}

	/**
	 * Adds a matched writer for this RTPSReader.
	 * 
	 * @param writerData
	 * @return WriterProxy
	 */
	public WriterProxy addMatchedWriter(PublicationData writerData) {
		LocatorPair locators = getLocators(writerData);
		WriterProxy wp = new WriterProxy(writerData, locators, heartbeatSuppressionDuration);
		wp.preferMulticast(getConfiguration().preferMulticast());

		writerProxies.put(writerData.getKey(), wp);

		log.debug("[{}] Added matchedWriter {}, uc:{}, mc:{}", getEntityId(), writerData,
				wp.getUnicastLocator(), wp.getMulticastLocator());

		//sendAckNack(wp);

		return wp;
	}

	/**
	 * Removes all the matched writers that have a given GuidPrefix
	 * 
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
	 * 
	 * @param writerData writer to remove. If corresponding writer does not exists, this method silently returns
	 */
	public void removeMatchedWriter(PublicationData writerData) {
		writerProxies.remove(writerData.getKey());

		log.debug("[{}] Removed matchedWriter {}", getEntityId(), writerData.getKey());
	}

	/**
	 * Gets all the matched writers of this RTPSReader.
	 * 
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
     * Checks, if this RTPSReader is already matched with a RTPSWriter
     * represented by given PublicationData.
     * 
     * @param pubData
     * @return true if matched
     */
    public boolean isMatchedWith(PublicationData pubData) {
    	return writerProxies.get(pubData.getKey()) != null;
    }

    /**
	 * Handle incoming HeartBeat message.
	 * 
	 * @param senderGuidPrefix
	 * @param hb
	 */
	void onHeartbeat(GuidPrefix senderGuidPrefix, Heartbeat hb) {
		log.debug("[{}] Got Heartbeat: #{} {}-{}, F:{}, L:{} from {}", getEntityId(), hb.getCount(),
				hb.getFirstSequenceNumber(), hb.getLastSequenceNumber(), hb.finalFlag(), hb.livelinessFlag(),
				senderGuidPrefix);

		WriterProxy wp = getWriterProxy(new Guid(senderGuidPrefix, hb.getWriterId()));
		if (wp != null) {
			if (wp.heartbeatReceived(hb)) {
				if (hb.livelinessFlag()) {
					wp.assertLiveliness();
				}

				if (isReliable()) { // Only reliable readers respond to
					// heartbeat
					boolean doSend = false;
					if (!hb.finalFlag()) { // if the FinalFlag is not set, then
						// the Reader must send an AckNack
						doSend = true;
					} else {
						if (wp.getGreatestDataSeqNum() < hb.getLastSequenceNumber()) {
							doSend = true;
						} else {
							log.trace("[{}] Will no send AckNack, since my seq-num is {} and Heartbeat seq-num is {}",
									getEntityId(), wp.getGreatestDataSeqNum(), hb.getLastSequenceNumber());
						}
					}

					if (doSend) {
						sendAckNack(wp);
					}
				}
			}
		} 
		else {
			log.warn("[{}] Discarding Heartbeat from unknown writer {}, {}", getEntityId(), senderGuidPrefix,
					hb.getWriterId());
		}
	}

	/**
     * Handles Gap submessage by updating WriterProxy.
     * 
     * @param sourceGuidPrefix
     * @param gap
     */
    void handleGap(GuidPrefix sourcePrefix, Gap gap) {
    	Guid writerGuid = new Guid(sourcePrefix, gap.getWriterId());
    
    	WriterProxy wp = getWriterProxy(writerGuid);
    	if (wp != null) {
    		log.debug("[{}] Applying {}", getEntityId(), gap);
    		wp.applyGap(gap);
    	}
    }

    /**
	 * This methods is called by RTPSMessageReceiver to signal that a message reception has started.
	 * This method is called for the first message received for this RTPSReader.
	 * 
	 * @param msgId Id of the message
	 * @see #stopMessageProcessing(int)
	 */
    void startMessageProcessing(int msgId) {
        rCache.changesBegin(msgId);
    }

    /**
     * Handle incoming Data message. Data is unmarshalled and added to pending
     * samples. Once RTPSMessageHandler has finished with the whole RTPSMessage,
     * it will call stopMessageProcessing of each RTPSReader that has received
     * some Data messages.
     * 
     * @param sourcePrefix GuidPrefix of the remote participant sending Data message
     * @param data Data SubMessage
     * @param timeStamp timestamp of the data
     * @throws IOException
     * @see #stopMessageProcessing()
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    void createSample(int id, GuidPrefix sourcePrefix, Data data, Time timeStamp) throws IOException {
        Guid writerGuid = new Guid(sourcePrefix, data.getWriterId());

        WriterProxy wp = getWriterProxy(writerGuid);
        if (wp != null) {
            if (wp.acceptData(data.getWriterSequenceNumber())) {
                log.debug("[{}] Got Data: {}", getEntityId(), data.getWriterSequenceNumber());

                T obj = rCache.addChange(id, writerGuid, data, timeStamp, data.getStatusInfo());
                
                // TODO: get rid of pendingSamples. replace with rCache.
                pendingSamples.add(new Sample(writerGuid, obj, timeStamp, data.getStatusInfo()));
            } else {
                log.debug("[{}] Data was rejected: Data seq-num={}, proxy seq-num={}", getEntityId(),
                        data.getWriterSequenceNumber(), wp.getGreatestDataSeqNum());
            }
        } else {
            log.warn("[{}] Discarding Data from unknown writer {}, {}", getEntityId(), sourcePrefix,
                    data.getWriterId());
        }
    }

    
    
	/**
	 * This method is called by RTPSMessageReceiver to signal that a message reception is done.
	 * 
     * @param msgId Id of the message
     * @see #startMessageProcessing(int)
	 * @see #createSample(GuidPrefix, Data, Time)
	 */
	void stopMessageProcessing(int msgId) {
		LinkedList<Sample<T>> ll = new LinkedList<>();

		rCache.changesEnd(msgId);

		// TODO: get rid of pendingSamples. replace with rCache.
		ll.addAll(pendingSamples);
		pendingSamples.clear();

		log.trace("[{}] Got {} samples", getEntityId(), ll.size());

		if (ll.size() > 0) {
			for (RTPSListener<T> sl : listeners) {
				sl.onSamples(ll);
			}
		}
	}

	private void sendAckNack(WriterProxy wp) {
    	log.trace("[{}] Wait for heartbeat response delay: {} ms", getEntityId(), heartbeatResponseDelay);
    	getParticipant().waitFor(heartbeatResponseDelay);
    
    	Message m = new Message(getGuid().getPrefix());
    
    	AckNack an = createAckNack(wp); // If all the data is already received,
    	// set finalFlag to true,
    	an.finalFlag(wp.isAllReceived()); // otherwise false(response required)
    
    	m.addSubMessage(an);
    
    	GuidPrefix targetPrefix = wp.getGuid().getPrefix();
    
    	log.debug("[{}] Sending AckNack: #{} {}, F:{} to {}", getEntityId(), an.getCount(),
    			an.getReaderSNState(), an.finalFlag(), targetPrefix);
    
    	sendMessage(m, wp);
    	// sendMessage(m, targetPrefix);
    }

    private AckNack createAckNack(WriterProxy wp) {
    	// This is a simple AckNack, that can be optimized if store
    	// out-of-order data samples in a separate cache.
    
    	long seqNumFirst = wp.getGreatestDataSeqNum(); // Positively ACK all
    	// that we have..
    	int[] bitmaps = new int[] { 0 }; // Negatively ACK rest
    	SequenceNumberSet snSet = new SequenceNumberSet(seqNumFirst + 1, bitmaps);
    
    	AckNack an = new AckNack(getEntityId(), wp.getEntityId(), snSet, ++ackNackCount);
    
    	return an;
    }

    private WriterProxy getWriterProxy(Guid writerGuid) {
    	WriterProxy wp = writerProxies.get(writerGuid);
    
    	if (wp == null && EntityId.SPDP_BUILTIN_PARTICIPANT_WRITER.equals(writerGuid.getEntityId())) {
    		log.debug("[{}] Creating proxy for SPDP writer {}", getEntityId(), writerGuid);
    		PublicationData pd = new PublicationData(ParticipantData.BUILTIN_TOPIC_NAME,
    				ParticipantData.class.getName(), writerGuid, QualityOfService.getSPDPQualityOfService());
    		wp = new WriterProxy(pd, new LocatorPair(null, Locator.defaultDiscoveryMulticastLocator(getParticipant()
    				.getDomainId())), 0);
    
    		writerProxies.put(writerGuid, wp);
    	}
    
    	return wp;
    }

    private boolean isReliable() {
		QosReliability reliability = getQualityOfService().getReliability();
		return reliability.getKind() == QosReliability.Kind.RELIABLE;
	}
}
