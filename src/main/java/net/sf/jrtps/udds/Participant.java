package net.sf.jrtps.udds;

import java.io.Externalizable;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.crypto.NoSuchPaddingException;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.Marshaller;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.QualityOfService.PolicyListener;
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
import net.sf.jrtps.message.parameter.ContentFilterProperty;
import net.sf.jrtps.message.parameter.IdentityToken;
import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.QosHistory;
import net.sf.jrtps.message.parameter.QosPolicy;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.message.parameter.QosReliability.Kind;
import net.sf.jrtps.rtps.RTPSParticipant;
import net.sf.jrtps.rtps.RTPSReader;
import net.sf.jrtps.rtps.RTPSWriter;
import net.sf.jrtps.transport.TransportProvider;
import net.sf.jrtps.transport.UDPProvider;
import net.sf.jrtps.types.Duration;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.types.Locator;
import net.sf.jrtps.udds.security.AuthenticationPlugin;
import net.sf.jrtps.udds.security.JKSAuthenticationPlugin;
import net.sf.jrtps.udds.security.NoOpAuthenticationPlugin;
import net.sf.jrtps.udds.security.ParticipantStatelessMessage;
import net.sf.jrtps.udds.security.ParticipantStatelessMessageMarshaller;
import net.sf.jrtps.util.Watchdog;

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

	private final Configuration config;
	private final HashMap<Class<?>, Marshaller<?>> marshallers = new HashMap<>();
	private final RTPSParticipant rtps_participant;

	private EntityFactory entityFactory;
	private List<DataReader<?>> readers = new CopyOnWriteArrayList<>();
	private List<DataWriter<?>> writers = new CopyOnWriteArrayList<>();

	private final Watchdog watchdog;

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

	private final List<Locator> discoveryLocators;
	private final List<Locator> userdataLocators;

	private List<EntityListener> entityListeners = new CopyOnWriteArrayList<>();

	private Guid guid;

	private JRTPSThreadFactory threadFactory;

	private final QualityOfService spdpQoS; // SPDP
	private final QualityOfService sedpQoS; // SEDP
	private final QualityOfService pmQoS;   // ParticipantMessage
	private final QualityOfService pvmQoS;  // ParticipantVolatileMessage

	//private QualityOfService participantQos;

	private AuthenticationPlugin authPlugin = null;

	{
		spdpQoS = QualityOfService.getSPDPQualityOfService(); 
		sedpQoS = QualityOfService.getSEDPQualityOfService();

		pmQoS = new QualityOfService();
		pmQoS.setPolicy(new QosReliability(QosReliability.Kind.RELIABLE, new Duration(0, 0)));
		pmQoS.setPolicy(new QosDurability(QosDurability.Kind.TRANSIENT_LOCAL));
		pmQoS.setPolicy(new QosHistory(QosHistory.Kind.KEEP_LAST, 1));

		pvmQoS = new QualityOfService();
		pvmQoS.setPolicy(new QosReliability(QosReliability.Kind.RELIABLE, new Duration(0, 0)));
		pvmQoS.setPolicy(new QosDurability(QosDurability.Kind.VOLATILE));
		pvmQoS.setPolicy(new QosHistory(QosHistory.Kind.KEEP_ALL, 1));
	}


	/**
	 * Create a Participant with domainId 0 and participantId -1.
	 */
	public Participant() {
		this(0, -1);
	}

	/**
	 * Create a Participant with given domainId and participantId -1.
	 *
	 * @param domainId domainId
	 */
	public Participant(int domainId) {
		this(domainId, -1);
	}

	/**
	 * Create a Participant with given domainId and participantId.
	 * 
	 * @param domainId domainId
	 * @param participantId
	 */
	public Participant(int domainId, int participantId) {
		this(domainId, participantId, null, null);
	}

	/**
	 * Create a Participant with given domainId and participantId. Participants
	 * with same domainId are able to communicate with each other. participantId
	 * is used to distinguish participants within this domain(and JVM). More
	 * specifically, domainId and participantId are used to select networking
	 * ports used by participant. If participantId is set to -1, participantId
	 * will be determined during starting of network receivers. First participantId
	 * (based on available port number) available will be used. 
	 * <p>
	 * Participant delegates creation of DataWriters and DataReaders to EntityFactory.
	 * By providing a custom EntityFactory, application can provide customized 
	 * DataReaders and DataWriters, including entities for builtin topics.
	 * 
	 * @param domainId domainId of this participant.
	 * @param participantId participantId of this participant.
	 * @param ef EntityFactory to be used. If ef is null, a default EntityFactory will be used.
	 * @param cfg Configuration used. If config is null, default Configuration is used.
	 */
	public Participant(int domainId, int participantId, EntityFactory ef, Configuration cfg) {
		logger.debug("Creating Participant for domain {}, participantId {}", domainId, participantId);

		this.entityFactory = ef != null ? ef : new EntityFactory();;
		this.config = cfg != null ? cfg : new Configuration();

		// UDPProvider is used 
		UDPProvider provider = new UDPProvider(config); 
		TransportProvider.registerTransportProvider(UDPProvider.PROVIDER_SCHEME, provider, 
				Locator.LOCATOR_KIND_UDPv4, Locator.LOCATOR_KIND_UDPv6);

		int corePoolSize = config.getIntProperty("jrtps.thread-pool.core-size", 20);
		int maxPoolSize = config.getIntProperty("jrtps.thread-pool.max-size", 20);
		threadFactory = new JRTPSThreadFactory(domainId);
		threadPoolExecutor = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
		threadPoolExecutor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
		threadPoolExecutor.setRemoveOnCancelPolicy(true);
		threadPoolExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

		logger.debug("Settings for thread-pool: core-size {}, max-size {}", corePoolSize, maxPoolSize);

		this.watchdog = new Watchdog(threadPoolExecutor);

		createUnknownParticipantData(domainId);

		try {		
			AuthenticationPlugin.registerPlugin(new NoOpAuthenticationPlugin(config));
			AuthenticationPlugin.registerPlugin(new JKSAuthenticationPlugin(config));
		} catch (InvalidKeyException | UnrecoverableKeyException
				| KeyStoreException | NoSuchAlgorithmException
				| CertificateException | NoSuchProviderException
				| SignatureException | NoSuchPaddingException | IOException e) {
			logger.warn("Failed to register JKSAuthenticationPlugin", e);
		}
				
		authPlugin = AuthenticationPlugin.getInstance(config.getAuthenticationPluginName());

		this.guid = authPlugin.getGuid();

		logger.debug("Created AuthenticationPlugin with name {}", config.getAuthenticationPluginName());

		rtps_participant = new RTPSParticipant(guid, domainId, participantId, threadPoolExecutor, 
				discoveredParticipants, authPlugin);

		this.livelinessManager = new WriterLivelinessManager(this);
		createSecurityEndpoints();
		authPlugin.init(this);		
		
		registerBuiltinMarshallers();
		createSPDPEntities();

		discoveryLocators = rtps_participant.getDiscoveryLocators();
		userdataLocators = rtps_participant.getUserdataLocators();

		logger.debug("Starting RTPS participant");
		rtps_participant.start();

		createSEDPEntities();
		
		@SuppressWarnings("unchecked")
		DataReader<ParticipantData> pdReader = 
				(DataReader<ParticipantData>) getReader(EntityId.SPDP_BUILTIN_PARTICIPANT_READER);
		pdReader.addSampleListener(new BuiltinParticipantDataListener(this, discoveredParticipants));

		createSPDPResender();

		livelinessManager.start();

		this.leaseManager = new ParticipantLeaseManager(this, discoveredParticipants);
		addRunnable(leaseManager);

		logger.info("Created Participant {}", Arrays.toString(getGuid().getBytes()));
	}

	private void createSecurityEndpoints() {
		registerSecureBuiltinMarshallers();
		QualityOfService statelessQos = new QualityOfService();
		statelessQos.setPolicy(new QosReliability(Kind.BEST_EFFORT, new Duration(0, 0)));

		DataWriter<ParticipantStatelessMessage> sWriter = 
				createDataWriter(ParticipantStatelessMessage.BUILTIN_TOPIC_NAME, 
						ParticipantStatelessMessage.class, ParticipantStatelessMessage.class.getSimpleName(), 
						statelessQos);
		DataReader<ParticipantStatelessMessage> sReader = 
				createDataReader(ParticipantStatelessMessage.BUILTIN_TOPIC_NAME, 
						ParticipantStatelessMessage.class, ParticipantStatelessMessage.class.getSimpleName(), 
						statelessQos);
	}

	private void registerSecureBuiltinMarshallers() {
		setMarshaller(ParticipantStatelessMessage.class, new ParticipantStatelessMessageMarshaller());
	}

	private void registerBuiltinMarshallers() {
		setMarshaller(ParticipantData.class, new ParticipantDataMarshaller());
		setMarshaller(ParticipantMessage.class, new ParticipantMessageMarshaller());
		setMarshaller(PublicationData.class, new PublicationDataMarshaller());
		setMarshaller(SubscriptionData.class, new SubscriptionDataMarshaller());
		setMarshaller(TopicData.class, new TopicDataMarshaller());
	}

	private void createSPDPEntities() {
		// ---- Create a Reader for SPDP -----------------------
		createDataReader(ParticipantData.BUILTIN_TOPIC_NAME,
				ParticipantData.class, ParticipantData.BUILTIN_TYPE_NAME, // ParticipantData.class.getName(),
				spdpQoS);

		// ---- Create a Writer for SPDP -----------------------
		DataWriter<ParticipantData> spdp_writer = createDataWriter(ParticipantData.BUILTIN_TOPIC_NAME,
				ParticipantData.class, ParticipantData.BUILTIN_TYPE_NAME, // ParticipantData.class.getName(),
				spdpQoS);

		// Add a matched reader for SPDP writer
		SubscriptionData sd = new SubscriptionData(ParticipantData.BUILTIN_TOPIC_NAME, ParticipantData.class.getName(),
				new Guid(GuidPrefix.GUIDPREFIX_UNKNOWN, EntityId.SPDP_BUILTIN_PARTICIPANT_READER), spdpQoS);
		spdp_writer.addMatchedReader(sd);
	}

	private void createSEDPEntities() {
		// ---- Create a Writers for SEDP ---------

		createDataWriter(PublicationData.BUILTIN_TOPIC_NAME, PublicationData.class, PublicationData.BUILTIN_TYPE_NAME, // PublicationData.class.getName(),
				sedpQoS);

		createDataWriter(SubscriptionData.BUILTIN_TOPIC_NAME, SubscriptionData.class,
				SubscriptionData.BUILTIN_TYPE_NAME, // SubscriptionData.class.getName(),
				sedpQoS);

		// NOTE: It is not mandatory to publish TopicData
		// createWriter(EntityId_t.SEDP_BUILTIN_TOPIC_WRITER, TopicData.BUILTIN_TOPIC_NAME, tMarshaller);


		// ---- Create a Readers for SEDP ---------
		DataReader<PublicationData> wdReader = createDataReader(PublicationData.BUILTIN_TOPIC_NAME,
				PublicationData.class, PublicationData.BUILTIN_TYPE_NAME, sedpQoS);
		wdReader.addSampleListener(new BuiltinPublicationDataListener(this, discoveredWriters));

		DataReader<SubscriptionData> rdReader = createDataReader(SubscriptionData.BUILTIN_TOPIC_NAME,
				SubscriptionData.class, SubscriptionData.BUILTIN_TYPE_NAME, sedpQoS);
		rdReader.addSampleListener(new BuiltinSubscriptionDataListener(this, discoveredReaders));

		// NOTE: It is not mandatory to publish TopicData, create reader anyway.
		// Maybe someone publishes TopicData.
		DataReader<TopicData> tReader = createDataReader(TopicData.BUILTIN_TOPIC_NAME, TopicData.class,
				TopicData.BUILTIN_TYPE_NAME, sedpQoS);
		tReader.addSampleListener(new BuiltinTopicDataListener(this));

		// Create entities for ParticipantMessage ---------------
		DataReader<ParticipantMessage> pmReader = createDataReader(ParticipantMessage.BUILTIN_TOPIC_NAME,
				ParticipantMessage.class, ParticipantMessage.class.getName(), pmQoS);
		pmReader.addSampleListener(new BuiltinParticipantMessageListener(this, readers));

		// Just create writer for ParticipantMessage, so that it will be listed
		// in builtin entities
		createDataWriter(ParticipantMessage.BUILTIN_TOPIC_NAME, ParticipantMessage.class,
				ParticipantMessage.class.getName(), pmQoS);
	}

	/**
	 * Gets the Guid of this Participant.
	 * @return guid
	 */
	public Guid getGuid() {
		return guid;
	}

	/**
	 * Create a new DataReader for given type T. DataReader is bound to a topic
	 * named c.getSimpleName(), which corresponds to class name of the argument.
	 * If Class <i>type</i> has a TypeName annotation set, it will be used to set
	 * the value of TypeName to be announced to remote entities. Otherwise
	 * TypeName is set to fully qualified class name.
	 * 
	 * @param type
	 * @return a DataReader
	 */
	public <T> DataReader<T> createDataReader(Class<T> type) {
		return createDataReader(type, new QualityOfService());
	}

	/**
	 * Create a new DataReader for given type T. DataReader is bound to a topic
	 * named type.getSimpleName(), which corresponds to class name of the argument.
	 * If Class <i>type</i> has a TypeName annotation set, it will be used to set
	 * the value of TypeName to be announced to remote entities. Otherwise
	 * TypeName is set to fully qualified class name.
	 * 
	 * @param type 
	 * @param qos QualityOfService used
	 * @return a DataReader
	 * @see Type
	 */
	public <T> DataReader<T> createDataReader(Class<T> type, QualityOfService qos) {
		return createDataReader(getTopicName(type), type, getTypeName(type), qos);
	}

	/**
	 * Create a DataReader. All the properties(topicName, type, typeName, qos) of the DataReader 
	 * is given in parameters.
	 * 
	 * @param topicName name of the topic
	 * @param type type of the DataReader
	 * @param typeName name of the type
	 * @param qos QualityOfService
	 * @return a DataReader
	 */
	public <T> DataReader<T> createDataReader(final String topicName, Class<T> type, final String typeName, final QualityOfService qos) {        
		logger.debug("Creating DataReader for topic {}, type {}, qos {}", topicName, typeName, qos);

		Marshaller<T> m = getMarshaller(type);
		EntityId eId = null;

		if (TopicData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			eId = EntityId.SEDP_BUILTIN_TOPIC_READER;
		} else if (SubscriptionData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			eId = EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_READER;
		} else if (PublicationData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			eId = EntityId.SEDP_BUILTIN_PUBLICATIONS_READER;
		} else if (ParticipantData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			eId = EntityId.SPDP_BUILTIN_PARTICIPANT_READER;
		} else if (ParticipantMessage.BUILTIN_TOPIC_NAME.equals(topicName)) {
			eId = EntityId.BUILTIN_PARTICIPANT_MESSAGE_READER;
		} else if (ParticipantStatelessMessage.BUILTIN_TOPIC_NAME.equals(topicName)) {
			eId = EntityId.BUILTIN_PARTICIPANT_STATELESS_READER;
		} else {
			int myIdx = userEntityIdx++;
			byte[] myKey = new byte[3];
			myKey[2] = (byte) (myIdx & 0xff); 
			myKey[1] = (byte) (myIdx >> 8 & 0xff);
			myKey[0] = (byte) (myIdx >> 16 & 0xff);

			int kind = 0x07; // User defined reader, with key, see 9.3.1.2
			// Mapping of the EntityId_t
			if (!m.hasKey()) {
				kind = 0x04; // User defined reader, no key
			}

			eId = new EntityId.UserDefinedEntityId(myKey, kind);
		}

		DataReader<?> rdr = getReader(eId);
		if (rdr != null) {
			logger.debug("A reader with same entityId {} already exists, will not create another one", eId);
			return (DataReader<T>) rdr;
		}

		UDDSReaderCache<T> rCache = new UDDSReaderCache<>(eId, m, qos, watchdog);
		final RTPSReader<T> rtps_reader = rtps_participant.createReader(eId, topicName, rCache, qos);
		rCache.setRTPSReader(rtps_reader);

		final DataReader<T> reader = entityFactory.createDataReader(this, type, typeName, rtps_reader);
		rCache.setCommunicationListeners(reader.communicationListeners);
		reader.setHistoryCache(rCache);
		readers.add(reader);

		writeSubscriptionData(reader);

		qos.addPolicyListener(new PolicyListener() {
			@Override
			public void policyChanged(QosPolicy policy) {
				writeSubscriptionData(reader);
			}
		});

		logger.debug("Created DataReader {}", reader.getGuid());
		
		return reader;
	}

	
	/**
	 * This method is called by createDataReader(...), or by DataReader.setContentFilter()
	 * @param reader
	 */
	void writeSubscriptionData(DataReader<?> reader) {
		RTPSReader<?> rtps_reader = reader.getRTPSReader();

		ContentFilterProperty cfp = reader.getContentFilterProperty();
		
		SubscriptionData sd = new SubscriptionData(reader.getTopicName(), reader.getTypeName(), 
				rtps_reader.getGuid(), cfp, rtps_reader.getQualityOfService());
		
		reader.setSubscriptionData(sd);
		
		if (rtps_reader.getEntityId().isUserDefinedEntity() || config.getPublishBuiltinEntities()) {
			@SuppressWarnings("unchecked")
			DataWriter<SubscriptionData> sw = (DataWriter<SubscriptionData>) getWritersForTopic(
					SubscriptionData.BUILTIN_TOPIC_NAME).get(0);
			sw.write(sd);
		}		
	}

	void removeDataReader(DataReader<?> dr) {
		readers.remove(dr);

		if (dr.getRTPSReader().getEntityId().isUserDefinedEntity() || config.getPublishBuiltinEntities()) {
			@SuppressWarnings("unchecked")
			DataWriter<SubscriptionData> sw = (DataWriter<SubscriptionData>) getWritersForTopic(
					SubscriptionData.BUILTIN_TOPIC_NAME).get(0);
			sw.write(dr.getSubscriptionData());
		}

		logger.debug("Removed DataReader {} for {}", dr.getGuid(), dr.getTopicName()); 
	}


	/**
	 * Creates a new DataWriter of given type. DataWriter is bound to a topic
	 * named c.getSimpleName(), which corresponds to class name of the argument.
	 * If Class <i>type</i> has a TypeName annotation set, it will be used to set
	 * the value of TypeName to be announced to remote entities. Otherwise
	 * TypeName is set to fully qualified class name.
	 * 
	 * 
	 * @param type A class, that is used with created DataWriter.
	 * @return a DataWriter
	 */
	public <T> DataWriter<T> createDataWriter(Class<T> type) {
		return createDataWriter(type, new QualityOfService());
	}

	/**
	 * Creates a new DataWriter of given type. DataWriter is bound to a topic
	 * named c.getSimpleName(), which corresponds to class name of the argument.
	 * If Class <i>type</i> has a Topic annotation set, it will be used to set
	 * the value of TypeName to be announced to remote entities. Otherwise
	 * TypeName is set to fully qualified class name.
	 * 
	 * 
	 * @param type A class, that is used with created DataWriter.
	 * @param qos QualityOfService
	 * @return a DataWriter
	 */
	public <T> DataWriter<T> createDataWriter(Class<T> type, QualityOfService qos) {
		return createDataWriter(getTopicName(type), type, getTypeName(type), qos);
	}

	/**
	 * Create a DataWriter. All the properties(topicName, type, typeName, qos) of the DataWriter 
	 * is given in parameters.
	 * 
	 * @param topicName name of the topic
	 * @param type type of the DataWriter
	 * @param typeName name of the type. typeName gets sent to remote readers. 
	 * @param qos QualityOfService
	 * @return a DataWriter
	 */
	public <T> DataWriter<T> createDataWriter(final String topicName, final Class<T> type, 
			final String typeName, final QualityOfService qos) {
		logger.debug("Creating DataWriter for topic {}, type {}, qos {}", topicName, typeName, qos);

		Marshaller<T> m = getMarshaller(type);
		EntityId eId = null;
		if (TopicData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			eId = EntityId.SEDP_BUILTIN_TOPIC_WRITER;
		} else if (SubscriptionData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			eId = EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER;
		} else if (PublicationData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			eId = EntityId.SEDP_BUILTIN_PUBLICATIONS_WRITER;
		} else if (ParticipantData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			eId = EntityId.SPDP_BUILTIN_PARTICIPANT_WRITER;
		} else if (ParticipantMessage.BUILTIN_TOPIC_NAME.equals(topicName)) {
			eId = EntityId.BUILTIN_PARTICIPANT_MESSAGE_WRITER;
		} else if (ParticipantStatelessMessage.BUILTIN_TOPIC_NAME.equals(topicName)) {
			eId = EntityId.BUILTIN_PARTICIPANT_STATELESS_WRITER;
		} else {
			int myIdx = userEntityIdx++;
			byte[] myKey = new byte[3];
			myKey[2] = (byte) (myIdx & 0xff);
			myKey[1] = (byte) (myIdx >> 8 & 0xff);
			myKey[0] = (byte) (myIdx >> 16 & 0xff);

			int kind = 0x02; // User defined writer, with key, see 9.3.1.2 Mapping of the EntityId_t
			if (!m.hasKey()) {
				kind = 0x03; // User defined writer, no key
			}

			eId = new EntityId.UserDefinedEntityId(myKey, kind);            
		}

		DataWriter<?> wr = getWriter(eId);
		if (wr != null) {
			logger.debug("A writer with same entityId {} already exists, will not create another one", eId);
			return (DataWriter<T>) wr;
		}

		UDDSWriterCache<T> wCache = new UDDSWriterCache<>(eId, m, qos, watchdog);
		RTPSWriter<T> rtps_writer = rtps_participant.createWriter(eId, topicName, wCache, qos);				
		final DataWriter<T> writer = entityFactory.createDataWriter(this, type, typeName, rtps_writer, wCache);

		wCache.setCommunicationListeners(writer.communicationListeners);

		writers.add(writer);
		livelinessManager.registerWriter(writer);

		writePublicationData(writer);

		qos.addPolicyListener(new PolicyListener() {
			@Override
			public void policyChanged(QosPolicy policy) {
				writePublicationData(writer);
			}
		});

		logger.debug("Created DataWriter {} for {}", writer.getGuid(), writer.getTopicName());

		return writer;
	}

	void writePublicationData(DataWriter writer) {
		RTPSWriter rtps_writer = writer.getRTPSWriter();
		PublicationData wd = new PublicationData(writer.getTopicName(), writer.getTypeName(), 
				rtps_writer.getGuid(), rtps_writer.getQualityOfService());
		writer.setPublicationData(wd);

		if (rtps_writer.getEntityId().isUserDefinedEntity() || config.getPublishBuiltinEntities()) {
			@SuppressWarnings("unchecked")
			DataWriter<PublicationData> pw = (DataWriter<PublicationData>) getWritersForTopic(
					PublicationData.BUILTIN_TOPIC_NAME).get(0);
			pw.write(wd);
		}
	}
	
	
	void removeDataWriter(DataWriter<?> dw) {
		dw.getRTPSWriter().close();
		writers.remove(dw);
		livelinessManager.unregisterWriter(dw);

		if (dw.getRTPSWriter().getEntityId().isUserDefinedEntity() || config.getPublishBuiltinEntities()) {
			@SuppressWarnings("unchecked")
			DataWriter<PublicationData> pw = (DataWriter<PublicationData>) getWritersForTopic(
					PublicationData.BUILTIN_TOPIC_NAME).get(0);
			pw.dispose(dw.getPublicationData());
		}

		logger.debug("Removed DataWriter {} for {}", dw.getGuid(), dw.getTopicName());
	}



	private String getTypeName(Class<?> c) {
		Type ta = c.getAnnotation(Type.class);
		String typeName;

		if (ta != null) {
			typeName = ta.typeName();
		}
		else {
			typeName = c.getName();
		}

		return typeName;
	}

	private String getTopicName(Class<?> c) {
		Type ta = c.getAnnotation(Type.class);
		String topicName = null;

		if (ta != null) {
			topicName = ta.topicName();
		}

		if (topicName == null || topicName.length() == 0) {
			topicName = c.getSimpleName();
		}

		return topicName;
	}

	/**
	 * Sets a type specific Marshaller. When creating entities, a type specific Marshaller is preferred 
	 * over other matching Marshallers. 
	 * 
	 * @param type a Class, that will be associated with given Marshaller 
	 * @param m Marshaller associated with given type
	 */
	public <T> void setMarshaller(Class<T> type, Marshaller<T> m) {
		marshallers.put(type, m);
	}

	/**
	 * Sets the EntityFactory of this Participant to given one. All the entities created after setting
	 * of EntityFactory will be created by that EntityFactory. Previously created entities remain as 
	 * before.
	 * <p>
	 * This method provides means to create entities by different EntityFactories. For example, one might want
	 * to create builtin entities with default EntityFactory, and application entities with customized EntityFactory.
	 * 
	 * @param ef EntityFactory to set
	 */
	public void setEntityFactory(EntityFactory ef) {
		entityFactory = ef;
	}

	/**
	 * Asserts liveliness of writers, whose QosLiveliness kind is MANUAL_BY_PARTICIPANT.
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
		rtps_participant.close();
		threadPoolExecutor.shutdown(); // won't accept new tasks, remaining tasks keeps on running.

		threadPoolExecutor.shutdownNow(); // Shutdown now.
		threadFactory.stopThreads();
	}

	/**
	 * Get a Marshaller for given type. If no explicit Marshaller is found for
	 * type, a default Marshaller is returned
	 * 
	 * @param type
	 * @return Marshaller
	 */
	@SuppressWarnings("unchecked")
	private <T> Marshaller<T> getMarshaller(Class<T> type) {
		Marshaller<?> m = marshallers.get(type);
		if (m == null) {
			if (Externalizable.class.isAssignableFrom(type)) {
				m = new ExternalizableMarshaller((Class<? extends Externalizable>) type);
			} else if (Serializable.class.isAssignableFrom(type)) {
				m = new SerializableMarshaller(type);
			} else {
				logger.error("No marshaller registered for {} and it is not Serializable or Externalizable", type);

				throw new IllegalArgumentException("No marshaller found for " + type);
			}
		}

		return (Marshaller<T>) m;
	}

	/**
	 * Get the RTPSParticipant
	 * 
	 * @return RTPSParticipant
	 */
	RTPSParticipant getRTPSParticipant() {
		return rtps_participant;
	}

	Configuration getConfiguration() {
		return config;
	}

	private ParticipantData createSPDPParticipantData() {
		int epSet = createEndpointSet();

		if (authPlugin != null) {
			IdentityToken iToken = authPlugin.getIdentityToken();
			return new ParticipantData(rtps_participant.getGuid().getPrefix(), epSet,
					discoveryLocators, userdataLocators, 
					iToken, null, spdpQoS); // TODO: PermissionsToken						
		}
		else {
			return new ParticipantData(rtps_participant.getGuid().getPrefix(), epSet,
					discoveryLocators, userdataLocators, spdpQoS);			
		}
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

	@SuppressWarnings("unchecked")
	private void createSPDPResender() {        
		final DataWriter<ParticipantData> spdp_writer = 
				(DataWriter<ParticipantData>) getWriter(EntityId.SPDP_BUILTIN_PARTICIPANT_WRITER);

		Runnable resendRunnable = new Runnable() {
			@Override
			public void run() {
				logger.debug("starting SPDP resend");
				ParticipantData pd = createSPDPParticipantData();

				spdp_writer.write(pd);
			}
		};

		Duration period = config.getSPDPResendPeriod();

		logger.debug("[{}] Starting SPDP announce thread with period {}", 
				rtps_participant.getGuid().getEntityId(), period);

		threadPoolExecutor.scheduleAtFixedRate(resendRunnable, 0, period.asMillis(), TimeUnit.MILLISECONDS);
		ParticipantData pd = createSPDPParticipantData();

		spdp_writer.write(pd);
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
	 * @param readerId EntityId of the Reader
	 * @return DataReader, or null if not found
	 */
	public DataReader<?> getReader(EntityId readerId) {
		for (DataReader<?> reader : readers) {
			if (reader.getRTPSReader().getEntityId().equals(readerId)) {
				return reader;
			}
		}

		return null;
	}

	/**
	 * Finds a Writer with given entity id.
	 * 
	 * @param writerId EntityId of the Writer
	 * @return DataWriter, or null if not found
	 */
	public DataWriter<?> getWriter(EntityId writerId) {
		for (DataWriter<?> writer : writers) {
			if (writer.getRTPSWriter().getEntityId().equals(writerId)) {
				return writer;
			}
		}

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
	 * Gets DataReaders created by this Participant.
	 * @return a List of DataReaders
	 */
	public List<DataReader<?>> getReaders() {
		return readers;
	}

	/**
	 * Gets DataWriters created by this Participant.
	 * @return a List of DataWriters
	 */
	public List<DataWriter<?>> getWriters() {
		return writers;
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
	 * Gets a reader for given type-
	 * 
	 * @param type Type of the reader
	 * @return DataReader<T>
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
	 * @param type Type of the writer
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
			if (prefix.equals(sd.getBuiltinTopicKey().getPrefix())) {
				__readers.add(sd);
			}
		}

		return __readers;
	}

	void ignoreParticipant(GuidPrefix prefix) {
		rtps_participant.ignoreParticipant(prefix);
	}

	private void createUnknownParticipantData(int domainId) {
		List<Locator> discoveryLocators = new LinkedList<>();

		List<URI> discoveryAnnounceURIs = config.getDiscoveryAnnounceURIs();
		for (URI uri : discoveryAnnounceURIs) {
			TransportProvider provider = TransportProvider.getInstance(uri.getScheme());
			if (provider != null) {
				Locator locator = provider.createDiscoveryLocator(uri, domainId); 
				discoveryLocators.add(locator);
			}
			else {
				logger.warn("No TranportProvider registered with scheme for {}", uri);
			}
		}

		logger.debug("Locators for discovery announcement: {}", discoveryLocators);

		ParticipantData pd = new ParticipantData(GuidPrefix.GUIDPREFIX_UNKNOWN, 0, discoveryLocators, 
				null, QualityOfService.getSPDPQualityOfService());

		// Set the lease duration to max integer. I.e. Never expires.
		pd.setLeaseDuration(new Duration(Integer.MAX_VALUE));

		discoveredParticipants.put(GuidPrefix.GUIDPREFIX_UNKNOWN, pd);
	}


	AuthenticationPlugin getAuthenticationPlugin() {
		return authPlugin;
	}
}
