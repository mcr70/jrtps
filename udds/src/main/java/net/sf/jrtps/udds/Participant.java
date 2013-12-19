package net.sf.jrtps.udds;

import java.net.SocketException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
	private Marshaller<?> defaultMarshaller;
	private final HashMap<String, Marshaller<?>> marshallers = new HashMap<>();
	private final RTPSParticipant rtps_participant;
	
	private List<DataReader> readers = new LinkedList<>();
	private List<DataWriter> writers = new LinkedList<>();
	
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
		defaultMarshaller = new JavaSerializableMarshaller();
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
		
		rtps_participant = new RTPSParticipant(domainId, participantId, threadPoolExecutor,
				meta_mcLoc, meta_ucLoc, mcLoc, ucLoc);
		rtps_participant.start();
		
		createBuiltinEntities();
		
		this.livelinessManager = new LivelinessManager(this);
		livelinessManager.start();
	}
	
	
	private void createBuiltinEntities() {
		// ----  Builtin marshallers  ---------------
		ParticipantDataMarshaller pdm = new ParticipantDataMarshaller();
		WriterDataMarshaller wdm = new WriterDataMarshaller();		
		ReaderDataMarshaller rdm = new ReaderDataMarshaller();
		TopicDataMarshaller tdm = new TopicDataMarshaller();		
		
		QualityOfService spdpQoS = new QualityOfService();
		QualityOfService sedpQoS = new QualityOfService();
		try {
			sedpQoS.setPolicy(new QosReliability(QosReliability.Kind.RELIABLE, new Duration(0, 0)));
		} catch (InconsistentPolicy e) {
			logger.error("Got InconsistentPolicy exception. This is an internal error", e);
		}

		// ----  Create a Writers for SEDP  ---------
		RTPSWriter<WriterData> pubWriter = rtps_participant.createWriter(EntityId.SEDP_BUILTIN_PUBLICATIONS_WRITER, 
				WriterData.BUILTIN_TOPIC_NAME, WriterData.class.getName(), wdm, sedpQoS);
		writers.add(new DataWriter<>(pubWriter));
		
		RTPSWriter<ReaderData> subWriter = rtps_participant.createWriter(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER, 
				ReaderData.BUILTIN_TOPIC_NAME, ReaderData.class.getName(), rdm, sedpQoS);
		writers.add(new DataWriter<>(subWriter));
		
		// NOTE: It is not mandatory to publish TopicData
		// createWriter(EntityId_t.SEDP_BUILTIN_TOPIC_WRITER, TopicData.BUILTIN_TOPIC_NAME, tMarshaller);


		// ----  Create a Reader for SPDP  -----------------------
		RTPSReader<ParticipantData> rtps_partReader = 
				rtps_participant.createReader(EntityId.SPDP_BUILTIN_PARTICIPANT_READER, 
						ParticipantData.BUILTIN_TOPIC_NAME, ParticipantData.class.getName(), pdm, spdpQoS);
		DataReader<ParticipantData> partReader = new DataReader<>(rtps_partReader);
		partReader.addListener(new BuiltinParticipantDataListener(rtps_participant, discoveredParticipants));
		readers.add(partReader);

		// ----  Create a Readers for SEDP  ---------
		RTPSReader<WriterData> rtps_pubReader = 
				rtps_participant.createReader(EntityId.SEDP_BUILTIN_PUBLICATIONS_READER, 
						WriterData.BUILTIN_TOPIC_NAME, WriterData.class.getName(), wdm, sedpQoS);
		DataReader<WriterData> pubReader = new DataReader<>(rtps_pubReader);
		pubReader.addListener(new BuiltinWriterDataListener(rtps_participant, discoveredWriters));
		readers.add(pubReader);
		
		RTPSReader<ReaderData> rtps_subReader = 
				rtps_participant.createReader(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_READER, 
						ReaderData.BUILTIN_TOPIC_NAME, ReaderData.class.getName(), rdm, sedpQoS);
		DataReader<ReaderData> subReader = new DataReader<>(rtps_subReader);
		subReader.addListener(new BuiltinReaderDataListener(rtps_participant, discoveredParticipants, discoveredReaders));
		readers.add(subReader);
		
		// NOTE: It is not mandatory to publish TopicData, create reader anyway. Maybe someone publishes TopicData.
		RTPSReader<TopicData> rtps_topicReader = 
				rtps_participant.createReader(EntityId.SEDP_BUILTIN_TOPIC_READER, 
						TopicData.BUILTIN_TOPIC_NAME, TopicData.class.getName(), tdm, sedpQoS);
		DataReader<TopicData> topicReader = new DataReader<>(rtps_topicReader);
		topicReader.addListener(new BuiltinTopicDataListener(rtps_participant));
		readers.add(topicReader);

		// ----  Create a Writer for SPDP  -----------------------
		RTPSWriter<ParticipantData> spdp_w = 
				rtps_participant.createWriter(EntityId.SPDP_BUILTIN_PARTICIPANT_WRITER, 
						ParticipantData.BUILTIN_TOPIC_NAME, ParticipantData.class.getName(), pdm, spdpQoS);
		writers.add(new DataWriter<>(spdp_w));

		ParticipantData pd = createSPDPParticipantData();
		spdp_w.write(pd);
		
		createSPDPResender(config.getSPDPResendPeriod(), spdp_w);
	}

	/**
	 * Create a new DataReader for given type T. DataReader is bound to a topic
	 * named c.getSimpleName(), which corresponds to class name of the argument. 
	 * Typename of the DataReader is set to fully qualified class name.
	 * @param c 
	 * @return a DataReader<T>
	 */
	public <T> DataReader<T> createDataReader(Class<T> c) {
		return createDataReader(c, new QualityOfService());
	} 

	public <T> DataReader<T> createDataReader(Class<T> c, QualityOfService qos) {
		return createDataReader(c.getSimpleName(), c, c.getName(), qos);
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
		Marshaller<?> m = getMarshaller(typeName);
		RTPSReader<T> rtps_reader = null;
		if (TopicData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			rtps_reader = rtps_participant.createReader(EntityId.SEDP_BUILTIN_TOPIC_READER, topicName, typeName, m, qos);
		}
		else if (ReaderData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			rtps_reader = rtps_participant.createReader(EntityId.SEDP_BUILTIN_SUBSCRIPTIONS_READER, topicName, typeName, m, qos);
		}
		else if (WriterData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			rtps_reader = rtps_participant.createReader(EntityId.SEDP_BUILTIN_PUBLICATIONS_READER, topicName, typeName, m, qos);
		}
		else if (ParticipantData.BUILTIN_TOPIC_NAME.equals(topicName)) {
			rtps_reader = rtps_participant.createReader(EntityId.SPDP_BUILTIN_PARTICIPANT_READER, topicName, typeName, m, qos);
		}
		else if (ParticipantMessage.BUILTIN_TOPIC_NAME.equals(topicName)) {
			rtps_reader = rtps_participant.createReader(EntityId.BUILTIN_PARTICIPANT_MESSAGE_READER, topicName, typeName, m, qos);
		}
		else {
			rtps_reader = rtps_participant.createReader(topicName, type, typeName, m, qos);			
		}
		
		DataReader<T> dr = new DataReader<T>(rtps_reader);
		readers.add(dr);
		
		logger.debug("Creating DataReader for topic {}, type {}", topicName, typeName);
		
		return dr;
	} 

	
	/**
	 * Creates a new DataWriter of given type. DataWriter is bound to a topic
	 * named c.getSimpleName(), which corresponds to class name of the argument. 
	 * Typename of the DataWriter is set to fully qualified class name.
	 * 
	 * @param c A class, that is used with created DataWriter.
	 * @return a DataWriter<T>
	 */
	public <T> DataWriter<T> createDataWriter(Class<T> c) {
		return createDataWriter(c, new QualityOfService());
	} 

	public <T> DataWriter<T> createDataWriter(Class<T> c, QualityOfService qos) {
		return createDataWriter(c.getSimpleName(), c, c.getName(), qos);
	} 
	
	/**
	 * Create DataWriter with given topicName and typeName.
	 * 
	 * @param topicName name of the topic
	 * @param type type of the DataWriter
	 * @param typeName name of the type
	 * @param qos QualityOfService
	 * @return a DataWriter<T>
	 */	
	public <T> DataWriter<T> createDataWriter(String topicName, Class<T> type, String typeName, QualityOfService qos) {
		Marshaller<?> m = getMarshaller(typeName);
		RTPSWriter<T> rtps_writer = rtps_participant.createWriter(topicName, type, typeName, m, qos);
		logger.debug("Creating DataWriter for topic {}, type {}", topicName, typeName);
		
		DataWriter<T> writer = new DataWriter<T>(rtps_writer);
		writers.add(writer);
		
		livelinessManager.registerWriter(writer);
		
		return writer;
	}

	
	/**
	 * Sets the default Marshaller. Default marshaller is used if no other Marshaller 
	 * could not be used.
	 * d
	 * @param m
	 */
	public void setDefaultMarshaller(Marshaller<?> m) {
		defaultMarshaller = m;
	}
	
	/**
	 * Sets a type specific Marshaller. When creating entities, a type specific Marshaller is
	 * preferred over default Marshaller.
	 * 
	 * @param typeName
	 * @param m
	 */
	public void setMarshaller(String typeName, Marshaller<?> m) {
		marshallers.put(typeName, m);
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
	 * @param typeName
	 * @return Marshaller
	 */
	private Marshaller<?> getMarshaller(String typeName) {
		Marshaller<?> m = marshallers.get(typeName);
		if (m == null) {
			m = defaultMarshaller;
		}
		
		return m;
	}


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
		for (DataReader dr : readers) {
			eps |= dr.getRTPSReader().endpointSetId();
		}
		for (DataWriter dw : writers) {
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
}
