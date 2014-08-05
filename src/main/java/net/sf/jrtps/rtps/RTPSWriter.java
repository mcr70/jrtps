package net.sf.jrtps.rtps;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.message.AckNack;
import net.sf.jrtps.message.Data;
import net.sf.jrtps.message.DataEncapsulation;
import net.sf.jrtps.message.Heartbeat;
import net.sf.jrtps.message.InfoTimestamp;
import net.sf.jrtps.message.Message;
import net.sf.jrtps.message.parameter.CoherentSet;
import net.sf.jrtps.message.parameter.DataWriterPolicy;
import net.sf.jrtps.message.parameter.Parameter;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.QosPolicy;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.message.parameter.StatusInfo;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RTPSWriter implements RTPS writer endpoint. RTPSWriter will not communicate
 * with unknown readers. It is expected that DDS implementation explicitly call
 * addMatchedReader(SubscriptionData) and removeMatchedReader(SubscriptionData).
 * 
 * Samples are written through an implementation of WriterCache, which will be
 * given when creating RTPSWriter with RTPSParticipant. When RTPSWriter needs to
 * write samples to RTPSReader, it will query WriterCache for the Samples.
 * 
 * @see WriterCache
 * @see RTPSParticipant#createWriter(EntityId, String, WriterCache, QualityOfService)
 * 
 * @author mcr70
 */
public class RTPSWriter<T> extends Endpoint {
    private static final Logger logger = LoggerFactory.getLogger(RTPSWriter.class);

    private final Map<Guid, ReaderProxy> readerProxies = new ConcurrentHashMap<>();

    private final WriterCache<T> writer_cache;
    private final int nackResponseDelay;
    private final int heartbeatPeriod;
    private final boolean pushMode;
    
    private int hbCount; // heartbeat counter. incremented each time hb is sent

    private ScheduledFuture<?> hbAnnounceTask;

    

    RTPSWriter(RTPSParticipant participant, EntityId entityId, String topicName, WriterCache<T> wCache,
            QualityOfService qos, Configuration configuration) {
        super(participant, entityId, topicName, qos, configuration);

        this.writer_cache = wCache;
        this.nackResponseDelay = configuration.getNackResponseDelay();
        this.heartbeatPeriod = configuration.getHeartbeatPeriod();
        this.pushMode = configuration.getPushMode();
        
        if (isReliable()) {
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    logger.debug("[{}] Starting periodical notification", getEntityId());
                    try {
                        // periodical notification is handled always as pushMode == false
                        notifyReaders(false);
                    } catch (Exception e) {
                        logger.error("Got exception while doing periodical notification", e);
                    }
                }
            };

            hbAnnounceTask = participant.scheduleAtFixedRate(r, heartbeatPeriod);
        }
    }

    /**
     * Get the BuiltinEndpointSet ID of this RTPSWriter.
     * 
     * @return 0, if this RTPSWriter is not builtin endpoint
     */
    public int endpointSetId() {
        return getEntityId().getEndpointSetId();
    }

    /**
     * Notify every matched RTPSReader. For reliable readers, a Heartbeat is
     * sent. For best effort readers Data is sent. This provides means to create
     * multiple changes, before announcing the state to readers.
     */
    public void notifyReaders() {
        notifyReaders(this.pushMode);
    }
    
    /**
     * Notify readers. Heartbeat announce thread calls this method always with 'false' as pushMode.
     * @param pushMode 
     */
    private void notifyReaders(boolean pushMode) {
        if (readerProxies.size() > 0) {
            logger.debug("[{}] Notifying {} matched readers of changes in history cache", getEntityId(),
                    readerProxies.size());

            for (ReaderProxy proxy : readerProxies.values()) {
                Guid guid = proxy.getSubscriptionData().getBuiltinTopicKey();
                notifyReader(guid, pushMode);
            }
        }
    }
        
    /**
     * Notifies a remote reader with given Guid of the changes available in this writer.
     * 
     * @param guid
     */
    public void notifyReader(Guid guid) {
        notifyReader(guid, this.pushMode);
    }
    
    /**
     * Notify a reader. Heartbeat announce thread calls this method always with 'false' as pushMode.
     * @param pushMode 
     */
    private void notifyReader(Guid guid, boolean pushMode) {
        ReaderProxy proxy = readerProxies.get(guid);

        if (proxy == null) {
            logger.warn("Will not notify, no proxy for {}", guid);
            return;
        }

        // Send HB only if proxy is reliable and we are not configured to be in pushMode
        if (proxy.isReliable() && !pushMode) {
            sendHeartbeat(proxy);
        } 
        else {
            long readersHighestSeqNum = proxy.getReadersHighestSeqNum();            
            sendData(proxy, readersHighestSeqNum);
            
            if (!proxy.isReliable()) {
            	// For best effort readers, update readers highest seqnum
                proxy.setReadersHighestSeqNum(writer_cache.getSeqNumMax());
            }
        }
    }

    /**
     * Assert liveliness of this writer. Matched readers are notified via
     * Heartbeat message of the liveliness of this writer.
     */
    public void assertLiveliness() {
        for (ReaderProxy proxy : readerProxies.values()) {
            sendHeartbeat(proxy, true); // Send Heartbeat regardless of readers QosReliability
        }
    }

    /**
     * Close this writer.
     */
    public void close() {
        if (hbAnnounceTask != null) {
            hbAnnounceTask.cancel(true);
        }

        readerProxies.clear();
    }

    /**
     * Add a matched reader.
     * 
     * @param readerData
     * @return ReaderProxy
     */
    public ReaderProxy addMatchedReader(SubscriptionData readerData) {
        LocatorPair locators = getLocators(readerData);
        ReaderProxy proxy = new ReaderProxy(readerData, locators, getConfiguration().getNackSuppressionDuration());
        proxy.preferMulticast(getConfiguration().preferMulticast());
        
        readerProxies.put(readerData.getBuiltinTopicKey(), proxy);

        QosDurability readerDurability = readerData.getQualityOfService().getDurability();

        if (QosDurability.Kind.VOLATILE == readerDurability.getKind()) {
            // VOLATILE readers are marked having received all the samples so far
            logger.trace("[{}] Setting highest seqNum to {} for VOLATILE reader", getEntityId(),
                    writer_cache.getSeqNumMax());

            proxy.setReadersHighestSeqNum(writer_cache.getSeqNumMax());
        } else {
            notifyReader(proxy.getGuid());
        }

        logger.debug("[{}] Added matchedReader {}", getEntityId(), readerData);
        return proxy;
    }

    /**
     * Removes all the matched writers that have a given GuidPrefix
     * 
     * @param prefix
     */
    public void removeMatchedReaders(GuidPrefix prefix) {
        for (ReaderProxy rp : readerProxies.values()) {
            if (prefix.equals(rp.getGuid().getPrefix())) {
                removeMatchedReader(rp.getSubscriptionData());
            }
        }
    }

    /**
     * Remove a matched reader.
     * 
     * @param readerData
     */
    public void removeMatchedReader(SubscriptionData readerData) {
        readerProxies.remove(readerData.getBuiltinTopicKey());
        logger.debug("[{}] Removed matchedReader {}, {}", getEntityId(), readerData.getBuiltinTopicKey());
    }

    /**
     * Gets all the matched readers of this RTPSWriter
     * 
     * @return a Collection of matched readers
     */
    public Collection<ReaderProxy> getMatchedReaders() {
        return readerProxies.values();
    }

    /**
     * Gets the matched readers owned by given remote participant.
     * 
     * @param prefix
     *            GuidPrefix of the remote participant
     * @return a Collection of matched readers
     */
    public Collection<ReaderProxy> getMatchedReaders(GuidPrefix prefix) {
        List<ReaderProxy> proxies = new LinkedList<>();
        for (Guid guid : readerProxies.keySet()) {
            if (guid.getPrefix().equals(prefix)) {
                proxies.add(readerProxies.get(guid));
            }
        }
        return proxies;
    }

    /**
     * Handle incoming AckNack message.
     * 
     * @param senderPrefix
     * @param ackNack
     */
    void onAckNack(GuidPrefix senderPrefix, AckNack ackNack) {
        logger.debug("[{}] Got AckNack: #{} {}, F:{} from {}", getEntityId(), ackNack.getCount(),
                ackNack.getReaderSNState(), ackNack.finalFlag(), senderPrefix);

        ReaderProxy proxy = readerProxies.get(new Guid(senderPrefix, ackNack.getReaderId()));
        if (proxy != null) {
            if (proxy.ackNackReceived(ackNack)) {
                logger.trace("[{}] Wait for nack response delay: {} ms", getEntityId(), nackResponseDelay);
                getParticipant().waitFor(nackResponseDelay);

                sendData(proxy, ackNack.getReaderSNState().getBitmapBase() - 1);
            }
        } 
        else {
            logger.warn("[{}] Discarding AckNack from unknown reader {}", getEntityId(), ackNack.getReaderId());
        }
    }

    /**
     * Send data to given participant & reader. readersHighestSeqNum specifies
     * which is the first data to be sent.
     * 
     * @param targetPrefix
     * @param readerId
     * @param readersHighestSeqNum
     */
    private void sendData(ReaderProxy proxy, long readersHighestSeqNum) {
        Message m = new Message(getGuid().getPrefix());
        LinkedList<Sample<T>> samples = writer_cache.getSamplesSince(readersHighestSeqNum);

        if (samples.size() == 0) {
            logger.debug("[{}] Remote reader already has all the data", getEntityId(),
                    proxy, readersHighestSeqNum);
            return;
        }
        
        long prevTimeStamp = 0;
        EntityId proxyEntityId = proxy.getEntityId();

        for (Sample<T> aSample : samples) {
            try {
                long timeStamp = aSample.getTimestamp();
                if (timeStamp > prevTimeStamp) {
                    InfoTimestamp infoTS = new InfoTimestamp(timeStamp);
                    m.addSubMessage(infoTS);
                }
                prevTimeStamp = timeStamp;

                logger.trace("Marshalling {}", aSample.getData());
                Data data = createData(proxyEntityId, proxy.expectsInlineQoS(), aSample);
                m.addSubMessage(data);
            } catch (IOException ioe) {
                logger.warn("[{}] Failed to add Sample to message", getEntityId(), ioe);
            }
        }

        // add HB at the end of data, see 8.4.15.4 Piggybacking HeartBeat submessages
        if (proxy.isReliable()) {
            Heartbeat hb = createHeartbeat(proxyEntityId);
            hb.finalFlag(false); // Reply needed
            m.addSubMessage(hb);
        }

        long firstSeqNum = samples.getFirst().getSequenceNumber();
        long lastSeqNum = samples.getLast().getSequenceNumber();
        
        logger.debug("[{}] Sending Data: {}-{} to {}", getEntityId(), firstSeqNum, lastSeqNum, proxy);

        boolean overFlowed = sendMessage(m, proxy);
        if (overFlowed) {
            logger.trace("Sending of Data overflowed. Sending HeartBeat to notify reader.");
            sendHeartbeat(proxy);
        }
    }

    private void sendHeartbeat(ReaderProxy proxy) {
        sendHeartbeat(proxy, false);
    }

    private void sendHeartbeat(ReaderProxy proxy, boolean livelinessFlag) {
        Message m = new Message(getGuid().getPrefix());
        Heartbeat hb = createHeartbeat(proxy.getEntityId());
        hb.livelinessFlag(livelinessFlag);
        m.addSubMessage(hb);

        logger.debug("[{}] Sending Heartbeat: #{} {}-{}, F:{}, L:{} to {}", getEntityId(), hb.getCount(),
                hb.getFirstSequenceNumber(), hb.getLastSequenceNumber(), hb.finalFlag(), hb.livelinessFlag(),
                proxy.getGuid());

        sendMessage(m, proxy);

        if (!livelinessFlag) {
            proxy.heartbeatSent();
        }
    }

    private Heartbeat createHeartbeat(EntityId entityId) {
        if (entityId == null) {
            entityId = EntityId.UNKNOWN_ENTITY;
        }

        Heartbeat hb = new Heartbeat(entityId, getEntityId(), writer_cache.getSeqNumMin(),
                writer_cache.getSeqNumMax(), hbCount++);

        return hb;
    }

    private Data createData(EntityId readerId, boolean expectsInlineQos, Sample<T> sample) throws IOException {
        DataEncapsulation dEnc = sample.getDataEncapsulation();
        ParameterList inlineQos = new ParameterList();

        if (expectsInlineQos) { // If reader expects inline qos, add them 
            Set<QosPolicy<?>> inlinePolicies = getQualityOfService().getInlinePolicies();
            for (QosPolicy<?> policy : inlinePolicies) {
                if (policy instanceof DataWriterPolicy) {
                    inlineQos.add((Parameter) policy); // TODO: safe cast, but ugly
                }
            }
        }
        
        CoherentSet cs = sample.getCoherentSet();
        
        if (cs != null) { // Add CoherentSet if present
            inlineQos.add(cs);
        }
        
        if (sample.hasKey()) { // Add KeyHash if present
            inlineQos.add(sample.getKey());
        }

        if (!ChangeKind.WRITE.equals(sample.getKind()) && sample.getKind() != null) { 
            // Add status info for operations other than WRITE
            inlineQos.add(new StatusInfo(sample.getKind()));
        }

        Data data = new Data(readerId, getEntityId(), sample.getSequenceNumber(), inlineQos, dEnc);

        return data;
    }

    /**
     * Checks, if a given change number has been acknowledged by every known
     * matched reader.
     * 
     * @param sequenceNumber sequenceNumber of a change to check
     * @return true, if every matched reader has acknowledged given change number
     */
    public boolean isAcknowledgedByAll(long sequenceNumber) {
        for (ReaderProxy proxy : readerProxies.values()) {
            if (proxy.isActive() && proxy.getReadersHighestSeqNum() < sequenceNumber) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks, if this RTPSWriter is already matched with a RTPSReader
     * represented by given SubscriptionData.
     * 
     * @param readerData
     * @return true if matched
     */
    public boolean isMatchedWith(SubscriptionData readerData) {
        return readerProxies.get(readerData.getBuiltinTopicKey()) != null;
    }

    boolean isReliable() {
        QosReliability policy = getQualityOfService().getReliability();

        return policy.getKind() == QosReliability.Kind.RELIABLE;
    }
}
