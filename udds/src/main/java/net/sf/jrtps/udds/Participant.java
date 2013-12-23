package net.sf.jrtps.udds;

import java.io.Serializable;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.Configuration;
import net.sf.jrtps.InconsistentPolicy;
import net.sf.jrtps.Marshaller;
import net.sf.jrtps.QualityOfService;
import net.sf.jrtps.RTPSParticipant;
import net.sf.jrtps.RTPSReader;
import net.sf.jrtps.RTPSWriter;
import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.ParticipantDataMarshaller;
import net.sf.jrtps.builtin.ParticipantMessage;
import net.sf.jrtps.builtin.ParticipantMessageMarshaller;
import net.sf.jrtps.builtin.ReaderData;
import net.sf.jrtps.builtin.ReaderDataMarshaller;
import net.sf.jrtps.builtin.TopicData;
import net.sf.jrtps.builtin.TopicDataMarshaller;
import net.sf.jrtps.builtin.WriterData;
import net.sf.jrtps.builtin.WriterDataMarshaller;
import net.sf.jrtps.message.parameter.BuiltinEndpointSet;
import net.sf.jrtps.message.parameter.QosReliability;
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

	private final ThreadPoolExecutor threadPoolExecutor;

	private final Configuration config = new Configuration();
	private final HashMap<Class<?>, Marshaller<?>> marshallers = new HashMap<>();
	private final RTPSParticipant rtps_participant;

	private List<DataReader<?>> readers = new LinkedList<>();
	private List<DataWriter<?>> writers = new LinkedList<>();

	/**
	 * Each user entity is assigned a unique number, this field is used for that purpose
	 */
	private volatile int userEntityIdx = 1;

	/**
	 * Maps that stores discovered participants. discovered participant is shared with
	 * all entities created by this participant. 
	 */
	private final HashMap<GuidPrefix, ParticipantData> discoveredParticipants =  new HashMap<>();
	private final HashMap<Guid, ReaderData> discoveredReaders = new HashMap<>();
	private final HashMap<Guid, WriterData> discoveredWriters = new HashMap<>();


	private final LivelinessManager livelinessManager;

	private Locator meta_mcLoc;
	private Locator meta_ucLoc;
	private Locator mcLoc;
	private Locator ucLoc;

	private List<EntityListener> entityListeners = new CopyOnWriteArrayList<>();


	/**
	 * Create a Participant with domainId 0 and participantId 0.
	 * @throws SocketException
	 */
	public Participant() throws SocketException {
		this(0, 0);
	} 

	/**
	 * Create a Participant with given domainId and participantId.
	 * Participants with same domainId are able to communicate with each other.
	 * participantId is used to distinguish participants within this domain(and JVM).
	 * More specifically, domainId and participantId are used to select networking ports used
	 * by participant.
	 * 
	 * @param domainId domainId of this participant.
	 * @param participantId participantId of this participant. 
	 * @throws SocketException
	 */
	public Participant(int domainId, int participantId) throws SocketException {
		logger.debug("Creating Participant for domain {}, participantId {}", domainId, participantId);

		int corePoolSize = config.getIntProperty("jrtps.thread-pool.core-size", 10);
		int maxPoolSize = config.getIntProperty("jrtps.thread-pool.max-size", 20);
		threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 5, TimeUnit.SECONDS, 
				new LinkedBlockingQueue<Runnable>(maxPoolSize));

		logger.debug("Settings for thread-pool: core-size {}, max-size {}", corePoolSize, maxPoolSize);

		meta_mcLoc = Locator.defaultDiscoveryMulticastLocator(domainId);
		meta_ucLoc = Locator.defaultMetatrafficUnicastLocator(domainId, participantId);
		mcLoc = Locator.defaultUserMulticastLocator(domainId);
		ucLoc = Locator.defaultUserUnicastLocator(domainId, participantId);

		HashSet<Locator> locators = new HashSet<>();
		locators.add(meta_mcLoc);
		locators.add(meta_ucLoc);
		locators.add(mcLoc);
		locators.add(ucLoc);
		
		rtps_participant = new RTPSParticipant(domainId, participantId, threadPoolExecutor, locators);
		rtps_participant.start();

		this.livelinessManager = new LivelinessManager(this);
		createBuiltinEntities();

		livelinessManager.start();
	}


	private void createBuiltinEntities() {
		// ----  Builtin marshallers  ---------------
		setMarshaller(ParticipantData.class, new ParticipantDataMarshaller());
		setMarshaller(ParticipantMessage.class, new ParticipantMessageMarshaller());
		setMarshaller(WriterData.class, new WriterDataMarshaller());
		setMarshaller(ReaderData.class, new ReaderDataMarshaller());
		setMarshaller(TopicData.class, new TopicDataMarshaller());

		QualityOfService spdpQoS = new QualityOfService();
		QualityOfService sedpQoS = new QualityOfService();

		try {
			sedpQoS.setPolicy(new QosReliability(QosReliability.Kind.RELIABLE, new Duration(0, 0)));
		} catch (InconsistentPolicy e) {
			logger.error("Got InconsistentPolicy exception. This is an internal error", e);
		}

		// ----  Create a Writers for SEDP  ---------
		DataWriter<WriterData> wdWriter = 
				createDataWriter(WriterData.BUILTIN_TOPIC_NAME, WriterData.class, WriterData.class.getName(), sedpQoS);
		writers.add(wdWriter);

		DataWriter<ReaderData> rdWriter = 
				createDataWriter(ReaderData.BUILTIN_TOPIC_NAME, ReaderData.class, ReaderData.class.getName(), sedpQoS);
		writers.add(rdWriter);


		// NOTE: It is not mandatory to publish TopicData
		// createWriter(EntityId_t.SEDP_BUILTIN_TOPIC_WRITER, TopicData.BUILTIN_TOPIC_NAME, tMarshaller);

		// ----  Create a Reader for SPDP  -----------------------
		DataReader<ParticipantData> pdReader = 
				createDataReader(ParticipantData.BUILTIN_TOPIC_NAME, ParticipantData.class, ParticipantData.class.getName(), spdpQoS);
		pdReader.addListener(new BuiltinParticipantDataListener(this, discoveredParticipants));
		readers.add(pdReader);

		// ----  Create a Readers for SEDP  ---------
		DataReader<WriterData> wdReader = 
				createDataReader(WriterData.BUILTIN_TOPIC_NAME, WriterData.class, WriterData.class.getName(), sedpQoS);
		wdReader.addListener(new BuiltinWriterDataListener(this, discoveredWriters));
		readers.add(wdReader);

		DataReader<ReaderData> rdReader = 
				createDataReader(ReaderData.BUILTIN_TOPIC_NAME, ReaderData.class, ReaderData.class.getName(), sedpQoS);
		rdReader.addListener(new BuiltinReaderDataListener(this, discoveredParticipants, discoveredReaders));
		readers.add(rdReader);

		// NOTE: It is not mandatory to publish TopicData, create reader anyway. Maybe someone publishes TopicData.
		DataReader<TopicData> tReader = 
				createDataReader(TopicData.BUILTIN_TOPIC_NAME, TopicData.class, TopicData.class.getName(), sedpQoS);
		tReader.addListener(new BuiltinTopicDataListener(this));
		readers.add(tReader);


		// ----  Create a Writer for SPDP  -----------------------
		DataWriter<ParticipantData> pdWriter = 
				createDataWriter(ParticipantData.BUILTIN_TOPIC_NAME, ParticipantData.class, ParticipantData.class.getName(), spdpQoS);
		ParticipantData pd = createSPDPParticipantData();
		pdWriter.write(pd);

		createSPDPResender(config.getSPDPResendPeriod(), pdWriter.getRTPSWriter());
	}

	/**
	 * Create a new DataReader for given type T. DataReader is bound to a topic
	 * named c.getSimpleName(), which corresponds to class name of the argument. 
	 * Typename of the DataReader is set to fully qualified class name.
	 * 
	 * @param type 
	 * @return a DataReader<T>
	 */
	public <T> DataReader<T> createDataReader(Class<T> type) {
		return createDataReader(type, new QualityOfService());
	} 

	public <T> DataReader<T> createDataReader(Class<T> type, QualityOfService qos) {
		return createDataReader(type.getSimpleName(), type, type.getName(), qos);
	} 

	/**
	 * Create DataReader with given topicName and typeName.
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
		}
		else if (ReaderData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			rtps_reader = rtps_participant.createReader(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_READER, topicName, m, qos);
		}
		else if (WriterData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			rtps_reader = rtps_participant.createReader(EntityId.SEDP_BUILTIN_PUBLICATIONS_READER, topicName, m, qos);
		}
		else if (ParticipantData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			rtps_reader = rtps_participant.createReader(EntityId.SPDP_BUILTIN_PARTICIPANT_READER, topicName, m, qos);
		}
		else if (ParticipantMessage.BUILTIN_TOPIC_NAME.equals(topicName)) {
			rtps_reader = rtps_participant.createReader(EntityId.BUILTIN_PARTICIPANT_MESSAGE_READER, topicName, m, qos);
		}
		else {
			int myIdx = userEntityIdx++;
			byte[] myKey = new byte[3];
			myKey[0] = (byte) (myIdx & 0xff);
			myKey[1] = (byte) (myIdx >> 8 & 0xff);
			myKey[2] = (byte) (myIdx >> 16 & 0xff);

			int kind = 0x07; // User defined reader, with key, see 9.3.1.2 Mapping of the EntityId_t
			if (!m.hasKey()) {
				kind = 0x04; // User defined reader, no key
			}

			rtps_reader = rtps_participant.createReader(new EntityId.UserDefinedEntityId(myKey, kind), topicName, m, qos);			
		}

		DataReader<T> reader = new DataReader<T>(this, rtps_reader);
		readers.add(reader);

		@SuppressWarnings("unchecked")
		DataWriter<ReaderData> sw = (DataWriter<ReaderData>) getWritersForTopic(ReaderData.BUILTIN_TOPIC_NAME).get(0);
		ReaderData rd = new ReaderData(topicName, typeName, reader.getRTPSReader().getGuid(), qos);
		sw.write(rd);

		return reader;
	} 


	/**
	 * Creates a new DataWriter of given type. DataWriter is bound to a topic
	 * named c.getSimpleName(), which corresponds to class name of the argument. 
	 * Typename of the DataWriter is set to fully qualified class name.
	 * A default QualityOfService is used. 
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
	 * Create DataWriter with given topicName and typeName.
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
		}
		else if (ReaderData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			rtps_writer = rtps_participant.createWriter(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER, topicName, wCache, qos);
		}
		else if (WriterData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			rtps_writer = rtps_participant.createWriter(EntityId.SEDP_BUILTIN_PUBLICATIONS_WRITER, topicName, wCache, qos);
		}
		else if (ParticipantData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			rtps_writer = rtps_participant.createWriter(EntityId.SPDP_BUILTIN_PARTICIPANT_WRITER, topicName, wCache, qos);
		}
		else if (ParticipantMessage.BUILTIN_TOPIC_NAME.equals(topicName)) {
			rtps_writer = rtps_participant.createWriter(EntityId.BUILTIN_PARTICIPANT_MESSAGE_WRITER, topicName, wCache, qos);
		}
		else {
			int myIdx = userEntityIdx++;
			byte[] myKey = new byte[3];
			myKey[0] = (byte) (myIdx & 0xff);
			myKey[1] = (byte) (myIdx >> 8 & 0xff);
			myKey[2] = (byte) (myIdx >> 16 & 0xff);

			int kind = 0x02; // User defined writer, with key, see 9.3.1.2 Mapping of the EntityId_t
			if (!m.hasKey()) {
				kind = 0x03; // User defined writer, no key
			}

			rtps_writer = rtps_participant.createWriter(new EntityId.UserDefinedEntityId(myKey, kind), topicName, wCache, qos);
		}
		
		DataWriter<T> writer = new DataWriter<T>(this, rtps_writer, wCache);
		writers.add(writer);
		livelinessManager.registerWriter(writer);

		@SuppressWarnings("unchecked")
		DataWriter<WriterData> pw = (DataWriter<WriterData>) getWritersForTopic(WriterData.BUILTIN_TOPIC_NAME).get(0);
		WriterData wd = new WriterData(writer.getTopicName(), typeName, writer.getRTPSWriter().getGuid(), qos);
		pw.write(wd);
		
		return writer;
	}


//	/**
//	 * Sets the default Marshaller. Default marshaller is used if no other Marshaller 
//	 * could not be used.
//	 * d
//	 * @param m
//	 */
//	public void setDefaultMarshaller(Marshaller<?> m) {
//		defaultMarshaller = m;
//	}

	/**
	 * Sets a type specific Marshaller. When creating entities, a type specific Marshaller is
	 * preferred over default Marshaller. Default Marshaller is JavaSerializationMarshaller, which
	 * uses java serialization.
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
	 * Asserts liveliness of RTPSWriters, whose QosLiveliness kind is MANUAL_BY_PARTICIPANT.
	 * 
	 * @see net.sf.jrtps.message.parameter.QosLiveliness
	 */
	public void assertLiveliness() {
		livelinessManager.assertLiveliness();
	}

	/**
	 * Close this participant.
	 */
	public void close() {
		threadPoolExecutor.shutdown();
		rtps_participant.close();

		try {
			boolean terminated = threadPoolExecutor.awaitTermination(1, TimeUnit.SECONDS);
			if (!terminated) {
				threadPoolExecutor.shutdownNow();
			}
		} catch (InterruptedException e) {
		}
	} 

	/**
	 * Get a Marshaller for given type. If no explicit Marshaller is found for type,
	 * a default Marshaller is returned 
	 * 
	 * @param type
	 * @return Marshaller
	 */
	private Marshaller<?> getMarshaller(Class<?> type) {
		Marshaller<?> m = marshallers.get(type);
		if (m == null) {
			if (Serializable.class.isAssignableFrom(type)) {
				m = new JavaSerializableMarshaller(type);
			}
			else {
				logger.error("No marshaller registered for {} and it is not Serializable", type);
			
				throw new IllegalArgumentException("No marshaller found for " + type);
			}
		}

		return m;
	}

	/**
	 * Get the RTPSParticipant
	 * @return RTPSParticipant
	 */
	RTPSParticipant getRTPSParticipant() {
		return rtps_participant;
	}


	private ParticipantData createSPDPParticipantData() {
		int epSet = createEndpointSet();
		ParticipantData pd = new ParticipantData(rtps_participant.getGuid().prefix, 
				epSet, ucLoc,  mcLoc,  meta_ucLoc, meta_mcLoc);

		logger.debug("Created ParticipantData: {}", pd);

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

		logger.debug("{}", new BuiltinEndpointSet(eps));

		return eps;
	}

	private void createSPDPResender(final Duration period, final RTPSWriter<ParticipantData> spdp_w) {
		Runnable resendRunnable = new Runnable() {
			@Override
			public void run() {
				boolean running = true;
				while (running) {
					spdp_w.sendData(null, EntityId.SPDP_BUILTIN_PARTICIPANT_READER, 0);
					try {
						running = !threadPoolExecutor.awaitTermination(period.asMillis(), TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						running = false;
					}
				}
			}
		};

		logger.debug("[{}] Starting resend thread with period {}", 
				rtps_participant.getGuid().entityId, period);		

		threadPoolExecutor.execute(resendRunnable);
	}


	/**
	 * Adds a runnable to be run with this participants thread pool.
	 * @param runnable
	 */
	void addRunnable(Runnable runnable) {
		threadPoolExecutor.execute(runnable);
	}


	/**
	 * Finds a Reader with given entity id.
	 * @param readerId
	 * @return DataReader
	 */
	DataReader<?> getReader(EntityId readerId) {
		for (DataReader<?> reader : readers) {
			if (reader.getRTPSReader().getGuid().entityId.equals(readerId)) {
				return reader;
			}
		}

		return null;
	}
	
	/**
	 * Finds a Writer with given entity id.
	 * @param writerId
	 * @return DataWriter
	 */
	DataWriter<?> getWriter(EntityId writerId) {
		for (DataWriter<?> writer : writers) {
			if (writer.getRTPSWriter().getGuid().entityId.equals(writerId)) {
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
	 * @return a List of DataWriters, or empty List if no writers were found for given topic
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
	 * @return a List of DataReaderss, or empty List if no readers were found for given topic
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
	 * @param el
	 */
	public void addEntityListener(EntityListener el) {
		entityListeners.add(el);
	}	

	/**
	 * Gets EntityListeners
	 * @return a List of EntityListeners
	 */
	public List<EntityListener> getEntityListeners() {
		return entityListeners;
	}

	/**
	 * Waits for a given amount of milliseconds.
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
}
