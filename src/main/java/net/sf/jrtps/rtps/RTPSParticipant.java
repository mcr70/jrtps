package net.sf.jrtps.rtps;

import java.net.SocketException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.Marshaller;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.transport.UDPReceiver;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RTPSParticipant is the main entry point to RTPS (DDS) domain. Participant is
 * responsible for creating readers and writers and setting up network
 * receivers.
 * 
 * @author mcr70
 * 
 */
public class RTPSParticipant {
    private static final Logger log = LoggerFactory.getLogger(RTPSParticipant.class);

    private final Configuration config;
    private final ScheduledThreadPoolExecutor threadPoolExecutor;

    /**
     * Maps that stores discovered participants. discovered participant is
     * shared with all entities created by this participant.
     */
    private final Map<GuidPrefix, ParticipantData> discoveredParticipants;

    /**
     * A Set that stores network receivers for each locator we know. (For
     * listening purposes)
     */
    private Set<UDPReceiver> receivers = new HashSet<UDPReceiver>();

    private final List<RTPSReader<?>> readerEndpoints = new LinkedList<>();
    private final List<RTPSWriter<?>> writerEndpoints = new LinkedList<>();

    private final Guid guid;
    private final Set<Locator> locators;

    private RTPSMessageReceiver handler;

    private int domainId;

    /**
     * Creates a new participant with given domainId and participantId. Domain
     * ID and particiapnt ID is used to construct unicast locators to this
     * RTPSParticipant. In general, participants in the same domain get to know
     * each other through SPDP. Each participant has a uniques unicast locator
     * to access its endpoints.
     * 
     * @param guid Guid, that is assigned to this participant. Every entity created by this
     *        RTPSParticipant will share the GuidPrefix of this Guid. 
     * @param domainId Domain ID of the participant
     * @param locators a Set of Locators
     * @param config Configuration used
     */
    public RTPSParticipant(Guid guid, int domainId, ScheduledThreadPoolExecutor tpe, Set<Locator> locators,
            Map<GuidPrefix, ParticipantData> discoveredParticipants, Configuration config) {
        this.guid = guid;
        this.domainId = domainId; // TODO: We should get rid of domainId here

        this.threadPoolExecutor = tpe;
        this.locators = locators;
        this.discoveredParticipants = discoveredParticipants;
        this.config = config;
    }

    /**
     * Starts this Participant. All the configured endpoints are initialized.
     * 
     * @throws SocketException
     */
    public void start() throws SocketException {
        // TODO: We should have endpoints for TCP, InMemory, What else?
        // encrypted?, signed?
        // UDP is required by the specification.
        BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>(config.getMessageQueueSize());

        // NOTE: We can have only one MessageReceiver. pending samples concept
        // relies on it.
        handler = new RTPSMessageReceiver(this, queue);
        threadPoolExecutor.execute(handler);

        int bufferSize = config.getBufferSize();
        for (Locator loc : locators) {
            UDPReceiver receiver = new UDPReceiver(loc, queue, bufferSize);
            receivers.add(receiver);
            threadPoolExecutor.execute(receiver);
        }

        log.debug("{} receivers, {} readers and {} writers started", receivers.size(), readerEndpoints.size(),
                writerEndpoints.size());
    }

    /**
     * Creates a new RTPSReader.
     * 
     * @param eId
     *            EntityId of the reader
     * @param topicName
     *            Name of the topic
     * @param marshaller
     * @param qos
     *            QualityOfService
     * 
     * @return RTPSReader
     */
    public <T> RTPSReader<T> createReader(EntityId eId, String topicName, Marshaller<T> marshaller, 
            ReaderCache<T> rCache, QualityOfService qos) {
        RTPSReader<T> reader = new RTPSReader<T>(this, eId, topicName, marshaller, rCache, qos, config);
        reader.setDiscoveredParticipants(discoveredParticipants);

        readerEndpoints.add(reader);

        return reader;
    }

    /**
     * Creates a new RTPSWriter.
     * 
     * @param eId
     *            EntityId of the reader
     * @param topicName
     *            Name of the topic
     * @param wCache
     *            WriterCache
     * @param qos
     *            QualityOfService
     * 
     * @return RTPSWriter
     */
    public <T> RTPSWriter<T> createWriter(EntityId eId, String topicName, WriterCache<T> wCache, QualityOfService qos) {
        RTPSWriter<T> writer = new RTPSWriter<T>(this, eId, topicName, wCache, qos, config);
        writer.setDiscoveredParticipants(discoveredParticipants);

        writerEndpoints.add(writer);

        return writer;
    }

    /**
     * Close this RTPSParticipant. All the network listeners will be stopped and
     * all the history caches of all entities will be cleared.
     */
    public void close() {
        log.debug("Closing RTPSParticipant {}", guid);

        for (RTPSWriter<?> w : writerEndpoints) { // Closes periodical announce
                                                  // thread
            w.close();
        }

        // close network receivers
        for (UDPReceiver r : receivers) {
            r.close();
        }
    }

    /**
     * Gets the guid of this participant.
     * 
     * @return guid
     */
    public Guid getGuid() {
        return guid;
    }

    /**
     * Gets the domainId of this participant;
     * 
     * @return domainId
     */
    int getDomainId() {
        return domainId;
    }

    /**
     * Waits for a given amount of milliseconds.
     * 
     * @param millis
     * @return true, if timeout occured normally
     */
    boolean waitFor(int millis) {
        if (millis > 0) {
            try {
                return !threadPoolExecutor.awaitTermination(millis, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                log.debug("waitFor(...) was interrupted");
            }
        }

        return false;
    }

    /**
     * Schedules given Runnable to be executed at given rate.
     * 
     * @param r
     * @param millis
     *            Number of milliseconds between executions
     * @return ScheduledFuture
     */
    ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long millis) {
        return threadPoolExecutor.scheduleAtFixedRate(r, millis, millis, TimeUnit.MILLISECONDS);
    }

    /**
     * Finds a Reader with given entity id.
     * 
     * @param readerId
     * @return RTPSReader
     */
    private RTPSReader<?> getReader(EntityId readerId) {
        for (RTPSReader<?> reader : readerEndpoints) {
            if (reader.getGuid().getEntityId().equals(readerId)) {
                return reader;
            }
        }

        return null;
    }

    /**
     * Gets a Reader with given readerId. If readerId is null or
     * EntityId_t.UNKNOWN_ENTITY, a search is made to match with corresponding
     * writerId. I.e. If writer is SEDP_BUILTIN_PUBLICATIONS_WRITER, a search is
     * made for SEDP_BUILTIN_PUBLICATIONS_READER.
     * 
     * @param readerId
     * @param writerId
     * @return RTPSReader
     */
    RTPSReader<?> getReader(EntityId readerId, EntityId writerId) {
        if (readerId != null && !EntityId.UNKNOWN_ENTITY.equals(readerId)) {
            return getReader(readerId);
        }

        if (writerId.equals(EntityId.SEDP_BUILTIN_PUBLICATIONS_WRITER)) {
            return getReader(EntityId.SEDP_BUILTIN_PUBLICATIONS_READER);
        }

        if (writerId.equals(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER)) {
            return getReader(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_READER);
        }

        if (writerId.equals(EntityId.SEDP_BUILTIN_TOPIC_WRITER)) {
            return getReader(EntityId.SEDP_BUILTIN_TOPIC_READER);
        }

        if (writerId.equals(EntityId.SPDP_BUILTIN_PARTICIPANT_WRITER)) {
            return getReader(EntityId.SPDP_BUILTIN_PARTICIPANT_READER);
        }

        if (writerId.equals(EntityId.BUILTIN_PARTICIPANT_MESSAGE_WRITER)) {
            return getReader(EntityId.BUILTIN_PARTICIPANT_MESSAGE_READER);
        }

        log.warn("Failed to find RTPSReader for reader entity {} or matching writer entity {}", readerId, writerId);
        return null;
    }

    /**
     * Finds a Writer with given entity id.
     * 
     * @param writerId
     * @return RTPSWriter
     */
    private RTPSWriter<?> getWriter(EntityId writerId) {
        for (RTPSWriter<?> writer : writerEndpoints) {
            if (writer.getGuid().getEntityId().equals(writerId)) {
                return writer;
            }
        }

        return null;
    }

    RTPSWriter<?> getWriter(EntityId writerId, EntityId readerId) {
        if (writerId != null && !EntityId.UNKNOWN_ENTITY.equals(writerId)) {
            return getWriter(writerId);
        }

        if (readerId.equals(EntityId.SEDP_BUILTIN_PUBLICATIONS_READER)) {
            return getWriter(EntityId.SEDP_BUILTIN_PUBLICATIONS_WRITER);
        }

        if (readerId.equals(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_READER)) {
            return getWriter(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER);
        }

        if (readerId.equals(EntityId.SEDP_BUILTIN_TOPIC_READER)) {
            return getWriter(EntityId.SEDP_BUILTIN_TOPIC_WRITER);
        }

        if (readerId.equals(EntityId.SPDP_BUILTIN_PARTICIPANT_READER)) {
            return getWriter(EntityId.SPDP_BUILTIN_PARTICIPANT_WRITER);
        }

        if (readerId.equals(EntityId.BUILTIN_PARTICIPANT_MESSAGE_READER)) {
            return getWriter(EntityId.BUILTIN_PARTICIPANT_MESSAGE_WRITER);
        }

        log.warn("Failed to find Writer for writer {} or matching reader {}", writerId, readerId);
        return null;
    }

    /**
     * Ignores messages originating from given Participant 
     * @param prefix GuidPrefix of the participant to ignore
     */
    public void ignoreParticipant(GuidPrefix prefix) {
        handler.ignoreParticipant(prefix);
    }
}
