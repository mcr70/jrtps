package net.sf.jrtps.udds;

import java.io.Externalizable;
import java.io.Serializable;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.Marshaller;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.ParticipantDataMarshaller;
import net.sf.jrtps.builtin.ParticipantMessage;
import net.sf.jrtps.builtin.ParticipantMessageMarshaller;
import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.builtin.PublicationDataMarshaller;
import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.builtin.SubscriptionDataMarshaller;
import net.sf.jrtps.builtin.TopicData;
import net.sf.jrtps.builtin.TopicDataMarshaller;
import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.rtps.RTPSParticipant;
import net.sf.jrtps.rtps.RTPSReader;
import net.sf.jrtps.rtps.RTPSWriter;
import net.sf.jrtps.types.Duration;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.types.Locator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Participant acts as a communication endpoint of different data.
 * 
 * @author mcr70
 * 
 */
public class Participant {
    private static final Logger logger = LoggerFactory.getLogger(Participant.class);

    private final ScheduledThreadPoolExecutor threadPoolExecutor;

    private final Configuration config = new Configuration();
    private final HashMap<Class<?>, Marshaller<?>> marshallers = new HashMap<>();
    private final RTPSParticipant rtps_participant;

    private List<DataReader<?>> readers = new LinkedList<>();
    private List<DataWriter<?>> writers = new LinkedList<>();

    /**
     * Each user entity is assigned a unique number, this field is used for that
     * purpose
     */
    private volatile int userEntityIdx = 1;

    /**
     * Maps that stores discovered participants. discovered participant is
     * shared with all entities created by this participant.
     */
    private final Map<GuidPrefix, ParticipantData> discoveredParticipants = Collections
            .synchronizedMap(new HashMap<GuidPrefix, ParticipantData>());
    private final Map<Guid, SubscriptionData> discoveredReaders = Collections
            .synchronizedMap(new HashMap<Guid, SubscriptionData>());
    private final Map<Guid, PublicationData> discoveredWriters = Collections
            .synchronizedMap(new HashMap<Guid, PublicationData>());

    private final WriterLivelinessManager livelinessManager;
    private final ParticipantLeaseManager leaseManager;

    private Locator meta_mcLoc;
    private Locator meta_ucLoc;
    private Locator mcLoc;
    private Locator ucLoc;

    private List<EntityListener> entityListeners = new CopyOnWriteArrayList<>();

    /**
     * Create a Participant with domainId 0 and participantId 0.
     * 
     * @throws SocketException
     */
    public Participant() throws SocketException {
        this(0, 0);
    }

    /**
     * Create a Participant with given domainId and participantId. Participants
     * with same domainId are able to communicate with each other. participantId
     * is used to distinguish participants within this domain(and JVM). More
     * specifically, domainId and participantId are used to select networking
     * ports used by participant.
     * 
     * @param domainId
     *            domainId of this participant.
     * @param participantId
     *            participantId of this participant.
     * @throws SocketException
     */
    public Participant(int domainId, int participantId) throws SocketException {
        logger.debug("Creating Participant for domain {}, participantId {}", domainId, participantId);

        int corePoolSize = config.getIntProperty("jrtps.thread-pool.core-size", 20);
        int maxPoolSize = config.getIntProperty("jrtps.thread-pool.max-size", 20);
        threadPoolExecutor = new ScheduledThreadPoolExecutor(corePoolSize);
        threadPoolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        threadPoolExecutor.setRemoveOnCancelPolicy(true);

        logger.debug("Settings for thread-pool: core-size {}, max-size {}", corePoolSize, maxPoolSize);

        createUnknownParticipantData(domainId);

        meta_mcLoc = Locator.defaultDiscoveryMulticastLocator(domainId);
        meta_ucLoc = Locator.defaultMetatrafficUnicastLocator(domainId, participantId);
        mcLoc = Locator.defaultUserMulticastLocator(domainId);
        ucLoc = Locator.defaultUserUnicastLocator(domainId, participantId);

        HashSet<Locator> locators = new HashSet<>();
        locators.add(meta_mcLoc);
        locators.add(meta_ucLoc);
        locators.add(mcLoc);
        locators.add(ucLoc);

        rtps_participant = new RTPSParticipant(domainId, participantId, threadPoolExecutor, locators,
                discoveredParticipants);
        rtps_participant.start();

        this.livelinessManager = new WriterLivelinessManager(this);
        createBuiltinEntities();

        livelinessManager.start();

        this.leaseManager = new ParticipantLeaseManager(this, discoveredParticipants);
        addRunnable(leaseManager);
    }

    private void createBuiltinEntities() {
        // ---- Builtin marshallers ---------------
        setMarshaller(ParticipantData.class, new ParticipantDataMarshaller());
        setMarshaller(ParticipantMessage.class, new ParticipantMessageMarshaller());
        setMarshaller(PublicationData.class, new PublicationDataMarshaller());
        setMarshaller(SubscriptionData.class, new SubscriptionDataMarshaller());
        setMarshaller(TopicData.class, new TopicDataMarshaller());

        QualityOfService spdpQoS = QualityOfService.getSPDPQualityOfService(); // QoS
                                                                               // for
                                                                               // SPDP
        QualityOfService sedpQoS = QualityOfService.getSEDPQualityOfService(); // QoS
                                                                               // for
                                                                               // SEDP
        QualityOfService pmQoS = new QualityOfService(); // QoS for
                                                         // ParticipantMessages

        try {
            pmQoS.setPolicy(new QosReliability(QosReliability.Kind.RELIABLE, new Duration(0, 0)));
            pmQoS.setPolicy(new QosDurability(QosDurability.Kind.TRANSIENT_LOCAL));
            pmQoS.setPolicy(new QosHistory(QosHistory.Kind.KEEP_LAST, 1));
        } catch (InconsistentPolicy e) {
            logger.error("Got InconsistentPolicy. This is an internal error", e);
            throw new RuntimeException(e);
        }

        // ---- Create a Writers for SEDP ---------

        createDataWriter(PublicationData.BUILTIN_TOPIC_NAME, PublicationData.class, PublicationData.BUILTIN_TYPE_NAME, // PublicationData.class.getName(),
                sedpQoS);

        createDataWriter(SubscriptionData.BUILTIN_TOPIC_NAME, SubscriptionData.class,
                SubscriptionData.BUILTIN_TYPE_NAME, // SubscriptionData.class.getName(),
                sedpQoS);

        // NOTE: It is not mandatory to publish TopicData
        // createWriter(EntityId_t.SEDP_BUILTIN_TOPIC_WRITER,
        // TopicData.BUILTIN_TOPIC_NAME, tMarshaller);

        // ---- Create a Reader for SPDP -----------------------
        DataReader<ParticipantData> pdReader = createDataReader(ParticipantData.BUILTIN_TOPIC_NAME,
                ParticipantData.class, ParticipantData.BUILTIN_TYPE_NAME, // ParticipantData.class.getName(),
                spdpQoS);
        pdReader.addListener(new BuiltinParticipantDataListener(this, discoveredParticipants));

        // ---- Create a Readers for SEDP ---------
        DataReader<PublicationData> wdReader = createDataReader(PublicationData.BUILTIN_TOPIC_NAME,
                PublicationData.class, PublicationData.BUILTIN_TYPE_NAME, // PublicationData.class.getName(),
                sedpQoS);
        wdReader.addListener(new BuiltinPublicationDataListener(this, discoveredWriters));

        DataReader<SubscriptionData> rdReader = createDataReader(SubscriptionData.BUILTIN_TOPIC_NAME,
                SubscriptionData.class, SubscriptionData.BUILTIN_TYPE_NAME, // SubscriptionData.class.getName(),
                sedpQoS);
        rdReader.addListener(new BuiltinSubscriptionDataListener(this, discoveredReaders));

        // NOTE: It is not mandatory to publish TopicData, create reader anyway.
        // Maybe someone publishes TopicData.
        DataReader<TopicData> tReader = createDataReader(TopicData.BUILTIN_TOPIC_NAME, TopicData.class,
                TopicData.BUILTIN_TYPE_NAME, // TopicData.class.getName(),
                sedpQoS);
        tReader.addListener(new BuiltinTopicDataListener(this));

        // Create entities for ParticipantMessage ---------------
        DataReader<ParticipantMessage> pmReader = createDataReader(ParticipantMessage.BUILTIN_TOPIC_NAME,
                ParticipantMessage.class, ParticipantMessage.class.getName(), pmQoS);
        pmReader.addListener(new BuiltinParticipantMessageListener(this, readers));

        // Just create writer for ParticipantMessage, so that it will be listed
        // in builtin entities
        createDataWriter(ParticipantMessage.BUILTIN_TOPIC_NAME, ParticipantMessage.class,
                ParticipantMessage.class.getName(), pmQoS);

        // ---- Create a Writer for SPDP -----------------------
        DataWriter<ParticipantData> spdp_writer = createDataWriter(ParticipantData.BUILTIN_TOPIC_NAME,
                ParticipantData.class, ParticipantData.BUILTIN_TYPE_NAME, // ParticipantData.class.getName(),
                spdpQoS);
        // TODO: Need to add GuidPrefix.UNKNOWN also to discovered participants,
        // with
        // leaseTime forever.

        // Add a matched reader for SPDP writer
        SubscriptionData sd = new SubscriptionData(ParticipantData.BUILTIN_TOPIC_NAME, ParticipantData.class.getName(),
                new Guid(GuidPrefix.GUIDPREFIX_UNKNOWN, EntityId.SPDP_BUILTIN_PARTICIPANT_READER), spdpQoS);
        spdp_writer.getRTPSWriter().addMatchedReader(sd);

        ParticipantData pd = createSPDPParticipantData();
        logger.debug("Created ParticipantData: {}", pd);
        
        spdp_writer.write(pd);

        createSPDPResender(config.getSPDPResendPeriod(), spdp_writer);
    }

    /**
     * Create a new DataReader for given type T. DataReader is bound to a topic
     * named c.getSimpleName(), which corresponds to class name of the argument.
     * Typename of the DataReader is set to fully qualified class name.
     * QualityOfService with default values will be used.
     * 
     * @param type
     * @return a DataReader<T>
     */
    public <T> DataReader<T> createDataReader(Class<T> type) {
        return createDataReader(type, new QualityOfService());
    }

    /**
     * Create a new DataReader for given type T. DataReader is bound to a topic
     * named c.getSimpleName(), which corresponds to class name of the argument.
     * Typename of the DataReader is set to fully qualified class name.
     * 
     * @param type 
     * @param qos QualityOfService used
     * @return
     */
    public <T> DataReader<T> createDataReader(Class<T> type, QualityOfService qos) {
        return createDataReader(type.getSimpleName(), type, type.getName(), qos);
    }

    /**
     * Create a DataReader. All the properties(topicName, type, typeName, qos) of the DataReader 
     * is given in parameters.
     * 
     * @param topicName name of the topic
     * @param type type of the DataReader
     * @param typeName name of the type
     * @param qos QualityOfService
     * @return a DataReader<T>
     */
    public <T> DataReader<T> createDataReader(String topicName, Class<T> type, String typeName, QualityOfService qos) {
        logger.debug("Creating DataReader for topic {}, type {}", topicName, typeName);

        Marshaller<?> m = getMarshaller(type);
        RTPSReader<T> rtps_reader = null;
        if (TopicData.BUILTIN_TOPIC_NAME.equals(topicName)) {
            rtps_reader = rtps_participant.createReader(EntityId.SEDP_BUILTIN_TOPIC_READER, topicName, m, qos);
        } else if (SubscriptionData.BUILTIN_TOPIC_NAME.equals(topicName)) {
            rtps_reader = rtps_participant.createReader(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_READER, topicName, m, qos);
        } else if (PublicationData.BUILTIN_TOPIC_NAME.equals(topicName)) {
            rtps_reader = rtps_participant.createReader(EntityId.SEDP_BUILTIN_PUBLICATIONS_READER, topicName, m, qos);
        } else if (ParticipantData.BUILTIN_TOPIC_NAME.equals(topicName)) {
            rtps_reader = rtps_participant.createReader(EntityId.SPDP_BUILTIN_PARTICIPANT_READER, topicName, m, qos);
        } else if (ParticipantMessage.BUILTIN_TOPIC_NAME.equals(topicName)) {
            rtps_reader = rtps_participant.createReader(EntityId.BUILTIN_PARTICIPANT_MESSAGE_READER, topicName, m, qos);
        } else {
            int myIdx = userEntityIdx++;
            byte[] myKey = new byte[3];
            myKey[0] = (byte) (myIdx & 0xff);
            myKey[1] = (byte) (myIdx >> 8 & 0xff);
            myKey[2] = (byte) (myIdx >> 16 & 0xff);

            int kind = 0x07; // User defined reader, with key, see 9.3.1.2
                             // Mapping of the EntityId_t
            if (!m.hasKey()) {
                kind = 0x04; // User defined reader, no key
            }

            rtps_reader = rtps_participant.createReader(new EntityId.UserDefinedEntityId(myKey, kind), topicName, m,
                    qos);
        }

        DataReader<T> reader = new DataReader<T>(this, type, rtps_reader);
        readers.add(reader);

        @SuppressWarnings("unchecked")
        DataWriter<SubscriptionData> sw = (DataWriter<SubscriptionData>) getWritersForTopic(
                SubscriptionData.BUILTIN_TOPIC_NAME).get(0);
        SubscriptionData rd = new SubscriptionData(topicName, typeName, reader.getRTPSReader().getGuid(), qos);
        sw.write(rd);

        return reader;
    }

    /**
     * Creates a new DataWriter of given type. DataWriter is bound to a topic
     * named c.getSimpleName(), which corresponds to class name of the argument.
     * Typename of the DataWriter is set to fully qualified class name. A
     * default QualityOfService is used.
     * 
     * @param c A class, that is used with created DataWriter.
     * @return a DataWriter<T>
     */
    public <T> DataWriter<T> createDataWriter(Class<T> c) {
        return createDataWriter(c, new QualityOfService());
    }

    /**
     * Creates a new DataWriter of given type. DataWriter is bound to a topic
     * named c.getSimpleName(), which corresponds to class name of the argument.
     * Typename of the DataWriter is set to fully qualified class name.
     * 
     * @param c A class, that is used with created DataWriter.
     * @param qos QualityOfService
     * @return a DataWriter<T>
     */
    public <T> DataWriter<T> createDataWriter(Class<T> c, QualityOfService qos) {
        return createDataWriter(c.getSimpleName(), c, c.getName(), qos);
    }

    /**
     * Create a DataWriter. All the properties(topicName, type, typeName, qos) of the DataWriter 
     * is given in parameters.
     * 
     * @param topicName name of the topic
     * @param type type of the DataWriter
     * @param typeName name of the type. typeName gets sent to remote readers.
     * @param qos QualityOfService
     * @return a DataWriter<T>
     */
    public <T> DataWriter<T> createDataWriter(String topicName, Class<T> type, String typeName, QualityOfService qos) {
        logger.debug("Creating DataWriter for topic {}, type {}", topicName, typeName);

        Marshaller<?> m = getMarshaller(type);
        HistoryCache<T> wCache = new HistoryCache(m, qos);
        RTPSWriter<T> rtps_writer = null;
        if (TopicData.BUILTIN_TOPIC_NAME.equals(topicName)) {
            rtps_writer = rtps_participant.createWriter(EntityId.SEDP_BUILTIN_TOPIC_WRITER, topicName, wCache, qos);
        } else if (SubscriptionData.BUILTIN_TOPIC_NAME.equals(topicName)) {
            rtps_writer = rtps_participant.createWriter(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER, topicName, wCache,
                    qos);
        } else if (PublicationData.BUILTIN_TOPIC_NAME.equals(topicName)) {
            rtps_writer = rtps_participant.createWriter(EntityId.SEDP_BUILTIN_PUBLICATIONS_WRITER, topicName, wCache,
                    qos);
        } else if (ParticipantData.BUILTIN_TOPIC_NAME.equals(topicName)) {
            rtps_writer = rtps_participant.createWriter(EntityId.SPDP_BUILTIN_PARTICIPANT_WRITER, topicName, wCache,
                    qos);
        } else if (ParticipantMessage.BUILTIN_TOPIC_NAME.equals(topicName)) {
            rtps_writer = rtps_participant.createWriter(EntityId.BUILTIN_PARTICIPANT_MESSAGE_WRITER, topicName, wCache,
                    qos);
        } else {
            int myIdx = userEntityIdx++;
            byte[] myKey = new byte[3];
            myKey[0] = (byte) (myIdx & 0xff);
            myKey[1] = (byte) (myIdx >> 8 & 0xff);
            myKey[2] = (byte) (myIdx >> 16 & 0xff);

            int kind = 0x02; // User defined writer, with key, see 9.3.1.2
                             // Mapping of the EntityId_t
            if (!m.hasKey()) {
                kind = 0x03; // User defined writer, no key
            }

            rtps_writer = rtps_participant.createWriter(new EntityId.UserDefinedEntityId(myKey, kind), topicName,
                    wCache, qos);
        }

        DataWriter<T> writer = new DataWriter<T>(this, type, rtps_writer, wCache);
        writers.add(writer);
        livelinessManager.registerWriter(writer);

        @SuppressWarnings("unchecked")
        DataWriter<PublicationData> pw = (DataWriter<PublicationData>) getWritersForTopic(
                PublicationData.BUILTIN_TOPIC_NAME).get(0);
        PublicationData wd = new PublicationData(writer.getTopicName(), typeName, writer.getRTPSWriter().getGuid(), qos);
        pw.write(wd);

        return writer;
    }

    // /**
    // * Sets the default Marshaller. Default marshaller is used if no other
    // Marshaller
    // * could not be used.
    // * d
    // * @param m
    // */
    // public void setDefaultMarshaller(Marshaller<?> m) {
    // defaultMarshaller = m;
    // }

    /**
     * Sets a type specific Marshaller. When creating entities, a type specific
     * Marshaller is preferred over default Marshaller. Default Marshaller is
     * JavaSerializationMarshaller, which uses java serialization.
     * 
     * @see JavaSerializableMarshaller
     * 
     * @param type
     * @param m
     */
    public void setMarshaller(Class<?> type, Marshaller<?> m) {
        marshallers.put(type, m);
    }

    /**
     * Asserts liveliness of writers, whose QosLiveliness kind is
     * MANUAL_BY_PARTICIPANT.
     * 
     * @see net.sf.jrtps.message.parameter.QosLiveliness
     */
    public void assertLiveliness() {
        livelinessManager.assertLiveliness();
    }

    /**
     * Close this participant. Closing a participant shuts down all the threads
     * allocated.
     * 
     */
    public void close() {
        threadPoolExecutor.shutdown(); // won't accept new tasks, remaining
                                       // tasks keeps on running.
        rtps_participant.close();
        threadPoolExecutor.shutdownNow(); // Shutdown now.
    }

    /**
     * Get a Marshaller for given type. If no explicit Marshaller is found for
     * type, a default Marshaller is returned
     * 
     * @param type
     * @return Marshaller
     */
    private Marshaller<?> getMarshaller(Class<?> type) {
        Marshaller<?> m = marshallers.get(type);
        if (m == null) {
            if (Externalizable.class.isAssignableFrom(type)) {
                m = new JavaExternalizableMarshaller((Class<? extends Externalizable>) type);
            } else if (Serializable.class.isAssignableFrom(type)) {
                m = new JavaSerializableMarshaller(type);
            } else {
                logger.error("No marshaller registered for {} and it is not Serializable", type);

                throw new IllegalArgumentException("No marshaller found for " + type);
            }
        }

        return m;
    }

    /**
     * Get the RTPSParticipant
     * 
     * @return RTPSParticipant
     */
    RTPSParticipant getRTPSParticipant() {
        return rtps_participant;
    }

    private ParticipantData createSPDPParticipantData() {
        int epSet = createEndpointSet();
        ParticipantData pd = new ParticipantData(rtps_participant.getGuid().getPrefix(), epSet, ucLoc, mcLoc,
                meta_ucLoc, meta_mcLoc);

        return pd;
    }

    private int createEndpointSet() {
        int eps = 0;
        for (DataReader<?> dr : readers) {
            eps |= dr.getRTPSReader().endpointSetId();
        }
        for (DataWriter<?> dw : writers) {
            eps |= dw.getRTPSWriter().endpointSetId();
        }

        return eps;
    }

    private void createSPDPResender(final Duration period, final DataWriter<ParticipantData> spdp_writer) {

        Runnable resendRunnable = new Runnable() {
            //Guid guid = new Guid(GuidPrefix.GUIDPREFIX_UNKNOWN, EntityId.SPDP_BUILTIN_PARTICIPANT_READER);

            @Override
            public void run() {
                logger.debug("starting SPDP resend");
                ParticipantData pd = createSPDPParticipantData();
                
                spdp_writer.write(pd);
                //spdp_writer.getRTPSWriter().notifyReader(guid);
            }
        };

        logger.debug("[{}] Starting resend thread with period {}", rtps_participant.getGuid().getEntityId(), period);

        threadPoolExecutor.scheduleAtFixedRate(resendRunnable, 0, period.asMillis(), TimeUnit.MILLISECONDS);
        // threadPoolExecutor.execute(resendRunnable);
    }

    /**
     * Adds a runnable to be run with this participants thread pool.
     * 
     * @param runnable
     */
    void addRunnable(Runnable runnable) {
        threadPoolExecutor.execute(runnable);
    }

    /**
     * Finds a Reader with given entity id.
     * 
     * @param readerId
     * @return DataReader
     */
    DataReader<?> getReader(EntityId readerId) {
        for (DataReader<?> reader : readers) {
            if (reader.getRTPSReader().getGuid().getEntityId().equals(readerId)) {
                return reader;
            }
        }

        return null;
    }

    /**
     * Finds a Writer with given entity id.
     * 
     * @param writerId
     * @return DataWriter
     */
    DataWriter<?> getWriter(EntityId writerId) {
        for (DataWriter<?> writer : writers) {
            if (writer.getRTPSWriter().getGuid().getEntityId().equals(writerId)) {
                return writer;
            }
        }

        logger.warn("Could not find a writer with entityId {}", writerId);
        return null;
    }

    /**
     * Get local writers that write to a given topic.
     * 
     * @param topicName
     * @return a List of DataWriters, or empty List if no writers were found for
     *         given topic
     */
    List<DataWriter<?>> getWritersForTopic(String topicName) {
        List<DataWriter<?>> __writers = new LinkedList<>();
        for (DataWriter<?> w : writers) {
            if (w.getTopicName().equals(topicName)) {
                __writers.add(w);
            }
        }

        return __writers;
    }

    /**
     * Get local readers that read from a given topic.
     * 
     * @param topicName
     * @return a List of DataReaderss, or empty List if no readers were found
     *         for given topic
     */
    List<DataReader<?>> getReadersForTopic(String topicName) {
        List<DataReader<?>> __readers = new LinkedList<>();
        for (DataReader<?> r : readers) {
            if (r.getTopicName().equals(topicName)) {
                __readers.add(r);
            }
        }

        return __readers;
    }

    /**
     * Adds a new EntityListener
     * 
     * @param el
     */
    public void addEntityListener(EntityListener el) {
        entityListeners.add(el);
    }

    /**
     * Gets EntityListeners
     * 
     * @return a List of EntityListeners
     */
    public List<EntityListener> getEntityListeners() {
        return entityListeners;
    }

    /**
     * Waits for a given amount of milliseconds.
     * 
     * @param millis
     * @return true, if timeout occured normally
     */
    boolean waitFor(long millis) {
        if (millis > 0) {
            try {
                return !threadPoolExecutor.awaitTermination(millis, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // logger.debug("waitFor was interrputed", e);
                // Ignore. We are shutting down.
            }
        }

        return false;
    }

    /**
     * Gets a writer for given type-
     * 
     * @param type
     *            Type of the writer
     * @return DataWriter<T>
     */
    @SuppressWarnings("unchecked")
    <T> DataReader<T> getDataReader(Class<T> type) {
        for (DataReader<?> dr : readers) {
            if (dr.getType().equals(type)) {
                return (DataReader<T>) dr;
            }
        }

        return null;
    }

    /**
     * Gets a writer for given type-
     * 
     * @param type
     *            Type of the writer
     * @return DataWriter<T>
     */
    @SuppressWarnings("unchecked")
    <T> DataWriter<T> getDataWriter(Class<T> type) {
        for (DataWriter<?> dw : writers) {
            if (dw.getType().equals(type)) {
                return (DataWriter<T>) dw;
            }
        }

        return null;
    }

    /**
     * Handle lease expiration of given participant. This method removes matched
     * readers and writers belonging to given participant.
     * 
     * @param prefix
     */
    void handleParticipantLeaseExpiration(GuidPrefix prefix) {
        ParticipantData pd = discoveredParticipants.get(prefix);

        if (pd != null) {
            // Remove participant
            discoveredParticipants.remove(prefix);
            // NOTE: Alternative would be to mark matched writers/readers as
            // 'not alive'
            // This would help in the case where participant gets back online

            // Remove matched writers
            for (DataReader<?> dr : readers) {
                dr.getRTPSReader().removeMatchedWriters(prefix);
            }

            // Remove matched readers
            for (DataWriter<?> dw : writers) {
                dw.getRTPSWriter().removeMatchedReaders(prefix);
            }

            logger.debug("Notifying participant lost status for {} listeners, {}", entityListeners.size(), pd);
            for (EntityListener el : entityListeners) {
                el.participantLost(pd);
            }
        }
    }

    List<SubscriptionData> getDiscoveredReaders(GuidPrefix prefix) {
        List<SubscriptionData> __readers = new LinkedList<>();
        for (SubscriptionData sd : discoveredReaders.values()) {
            if (prefix.equals(sd.getKey().getPrefix())) {
                __readers.add(sd);
            }
        }

        return __readers;
    }

    void ignoreParticipant(GuidPrefix prefix) {
        rtps_participant.ignoreParticipant(prefix);
    }

    private void createUnknownParticipantData(int domainId) {
        ParticipantData pd = new ParticipantData(GuidPrefix.GUIDPREFIX_UNKNOWN, 0, null, null, null,
                Locator.defaultDiscoveryMulticastLocator(domainId));

        // Set the lease duration to max integer. I.e. Never expires.
        pd.setLeaseDuration(new Duration(Integer.MAX_VALUE));

        discoveredParticipants.put(GuidPrefix.GUIDPREFIX_UNKNOWN, pd);
    }
}