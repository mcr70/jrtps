package net.sf.jrtps.rtps;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.transport.Receiver;
import net.sf.jrtps.transport.TransportProvider;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.types.Locator;
import net.sf.jrtps.udds.security.AuthenticationPlugin;
import net.sf.jrtps.util.Watchdog;

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
    private static final Logger logger = LoggerFactory.getLogger(RTPSParticipant.class);

    private final Configuration config;
    private final ScheduledThreadPoolExecutor threadPoolExecutor;
    private final Watchdog watchdog;
    
    /**
     * Maps that stores discovered participants. discovered participant is
     * shared with all entities created by this participant.
     */
    private final Map<GuidPrefix, ParticipantData> discoveredParticipants;


    private final List<RTPSReader<?>> readerEndpoints = new CopyOnWriteArrayList<>();
    private final List<RTPSWriter<?>> writerEndpoints = new CopyOnWriteArrayList<>();

    private final LinkedList<Locator> discoveryLocators = new LinkedList<>();
    private final LinkedList<Locator> userdataLocators = new LinkedList<>();


    private final Guid guid;
    private RTPSMessageReceiver handler;

    private int domainId;
    private int participantId;

	private final AuthenticationPlugin aPlugin;


    /**
     * Creates a new participant with given domainId and participantId. Domain
     * ID and participant ID is used to construct unicast locators to this
     * RTPSParticipant. In general, participants in the same domain get to know
     * each other through SPDP. Each participant has a unique unicast locator
     * to access its endpoints.
     * 
     * @param guid Guid, that is assigned to this participant. Every entity created by this
     *        RTPSParticipant will share the GuidPrefix of this Guid. 
     * @param domainId Domain ID of the participant
     * @param participantId Participant ID of this participant. If set to -1, and port number is not given
     *        during starting of receivers, participantId will be determined based on the first
     *        suitable network socket.
     * @param tpe threadPoolExecutor
     * @param discoveredParticipants a Map that holds discovered participants
     * @param aPlugin AuthenticationPlugin 
     */
    public RTPSParticipant(Guid guid, int domainId, int participantId, ScheduledThreadPoolExecutor tpe, 
            Map<GuidPrefix, ParticipantData> discoveredParticipants, AuthenticationPlugin aPlugin) {
        this.guid = guid;
        this.domainId = domainId; 
        this.participantId = participantId;
        this.threadPoolExecutor = tpe;
		this.aPlugin = aPlugin;
        this.watchdog = new Watchdog(threadPoolExecutor);
        this.discoveredParticipants = discoveredParticipants;
        this.config = aPlugin.getConfiguration();
    }


    /**
     * Starts this Participant. All the configured endpoints are initialized.
     */
    public void start() {
        BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>(config.getMessageQueueSize());

        // NOTE: We can have only one MessageReceiver. pending samples concept
        // relies on it.
        handler = new RTPSMessageReceiver(aPlugin.getCryptoPlugin(), this, queue, config);
        threadPoolExecutor.execute(handler);

        logger.debug("Starting receivers for discovery");
        List<URI> discoveryURIs = config.getDiscoveryListenerURIs();
        int receiverCount = startReceiversForURIs(queue, discoveryURIs, true);

        logger.debug("Starting receivers for user data");
        List<URI> listenerURIs = config.getListenerURIs();
        receiverCount += startReceiversForURIs(queue, listenerURIs, false);

        logger.debug("{} receivers, {} readers and {} writers started", receiverCount,  
        		readerEndpoints.size(), writerEndpoints.size());
    }

    /**
     * Creates a new RTPSReader.
     * 
     * @param eId EntityId of the reader
     * @param topicName Name of the topic
     * @param rCache ReaderCache
     * @param qos QualityOfService
     * @param <T> Type of RTPSReader??
     * @return RTPSReader
     */
    public <T> RTPSReader<T> createReader(EntityId eId, String topicName, ReaderCache<T> rCache, QualityOfService qos) {
        RTPSReader<T> reader = new RTPSReader<T>(this, eId, topicName, rCache, qos, config);
        reader.setDiscoveredParticipants(discoveredParticipants);
		
        readerEndpoints.add(reader);

        return reader;
    }

    /**
     * Creates a new RTPSWriter.
     * 
     * @param eId EntityId of the reader
     * @param topicName Name of the topic
     * @param wCache WriterCache
     * @param qos QualityOfService
     * @param <T> Type of RTPSWriter
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
        logger.debug("Closing RTPSParticipant {}", guid);
        handler.close(); // Close RTPSMessageReceiver loop gracefully

        for (RTPSWriter<?> w : writerEndpoints) { // Closes periodical announce thread
            w.close();
        }

        writerEndpoints.clear();
        readerEndpoints.clear();
        
        // Let TransportProviders do cleanup
        Collection<TransportProvider> transportProviders = TransportProvider.getTransportProviders();
        for (TransportProvider tp : transportProviders) { 
        	tp.close(); 
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
     * Gets the Locators that can be used for discovery
     * @return a List of Locators
     */
    public List<Locator> getDiscoveryLocators() {
        return discoveryLocators;
    }

    /**
     * Gets the Locators that can be used for user data
     * @return a List of Locators
     */
    public List<Locator> getUserdataLocators() {
        return userdataLocators;
    }

    /**
     * Ignores messages originating from given Participant 
     * @param prefix GuidPrefix of the participant to ignore
     */
    public void ignoreParticipant(GuidPrefix prefix) {
        handler.ignoreParticipant(prefix);
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
                logger.debug("waitFor(...) was interrupted");
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
    
    ScheduledFuture<?> schedule(Runnable r, long delayInMillis) {
    	return threadPoolExecutor.schedule(r, delayInMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Gets a Reader with given readerId. If readerId is null or
     * EntityId_t.UNKNOWN_ENTITY, a search is made to match with corresponding
     * writerId. I.e. If writer is SEDP_BUILTIN_PUBLICATIONS_WRITER, a search is
     * made for SEDP_BUILTIN_PUBLICATIONS_READER.
     * 
     * @param readerId
     * @param sourceGuidPrefix 
     * @param writerId
     * @return RTPSReader
     */
    RTPSReader<?> getReader(EntityId readerId, GuidPrefix sourceGuidPrefix, EntityId writerId) {
        //logger.warn("getReader({}, {}, {}", readerId, sourceGuidPrefix, writerId);
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

        Guid writerGuid = new Guid(sourceGuidPrefix, writerId);
        if (EntityId.UNKNOWN_ENTITY.equals(readerId)) {
            StringBuffer sb = new StringBuffer();
            logger.debug("writer {} wants to talk to UNKNOWN_ENTITY", writerId);
            
            for (RTPSReader<?> r : readerEndpoints) {
                logger.trace("Check if reader {} is matched: {}", r.getEntityId(), r.isMatchedWith(writerGuid));
                sb.append(r.getEntityId() + " ");
                if (r.isMatchedWith(writerGuid)) {
                    logger.debug("Found reader {} that is matched with {}", r.getEntityId(), writerGuid);
                    return r; // TODO: we should return a List<RTPSReader>
                }
            }

            logger.trace("Known reader entities: {}", sb);
        }
        
        return null;
    }

    
    RTPSWriter<?> getWriter(EntityId writerId, GuidPrefix sourceGuidPrefix, EntityId readerId) {
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

        Guid readerGuid = new Guid(sourceGuidPrefix, readerId);
        if (EntityId.UNKNOWN_ENTITY.equals(writerId)) {
            for (RTPSWriter<?> writer : writerEndpoints) {
                if (writer.isMatchedWith(readerGuid)) {
                    return writer; // TODO: we should return a List<RTPSWriter>
                }
            }
        }
        
        logger.warn("None of the writers were matched with reader {}", readerGuid);        
        
        return null;
    }

    
    /**
     * Gets the Watchdog of this RTPSParticipant.
     * @return Watchdog
     */
    Watchdog getWatchdog() {
        return watchdog;
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

    private int startReceiversForURIs(BlockingQueue<byte[]> queue, List<URI> listenerURIs, 
            boolean discovery) {
    	int count = 0;
        for (URI uri : listenerURIs) {
            TransportProvider provider = TransportProvider.getProviderForScheme(uri.getScheme());

            if (provider != null) {
                try {
                    logger.debug("Starting receiver for {}", uri);
                    Locator locator = provider.createLocator(uri, domainId, participantId, discovery);
                    Receiver receiver = provider.getReceiver(locator, queue);

                    addLocator(locator, discovery);
                    threadPoolExecutor.execute(receiver);
                    count++;
                } catch (IOException ioe) {
                    logger.warn("Failed to start receiver for URI {}", uri, ioe);
                }
            }
            else {
                logger.warn("Unknown scheme for URI {}", uri);
            }
        }
        
        return count;
    }

    /** 
     * Assigns Receivers Locator to proper field
     * @param loc
     * @param discovery
     */
    private void addLocator(Locator loc, boolean discovery) {
        if (discovery) {
            discoveryLocators.add(loc);
        }
        else {
            userdataLocators.add(loc);
        }    
    }

    List<RTPSReader<?>> getReaders() {
        return readerEndpoints;
    }
    List<RTPSWriter<?>> getWriters() {
        return writerEndpoints;
    }
    
    AuthenticationPlugin getAuthenticationPlugin() {
		return aPlugin;
	}
}
