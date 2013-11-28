package net.sf.jrtps;

import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.ParticipantDataMarshaller;
import net.sf.jrtps.builtin.ReaderData;
import net.sf.jrtps.builtin.ReaderDataMarshaller;
import net.sf.jrtps.builtin.TopicData;
import net.sf.jrtps.builtin.TopicDataMarshaller;
import net.sf.jrtps.builtin.WriterData;
import net.sf.jrtps.builtin.WriterDataMarshaller;
import net.sf.jrtps.message.parameter.BuiltinEndpointSet;
import net.sf.jrtps.message.parameter.QosReliability;
import net.sf.jrtps.transport.UDPReceiver;
import net.sf.jrtps.types.Duration_t;
import net.sf.jrtps.types.EntityId_t;
import net.sf.jrtps.types.GUID_t;
import net.sf.jrtps.types.GuidPrefix_t;
import net.sf.jrtps.types.Locator_t;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RTPSParticipant is the main entry point to RTPS (DDS) domain.
 * Participant is responsible for creating readers and writers and setting up
 * network receivers.
 * 
 * @author mcr70
 *
 */
public class RTPSParticipant {
	private static final Logger log = LoggerFactory.getLogger(RTPSParticipant.class);

	private final Configuration config = new Configuration();
	private final ThreadPoolExecutor threadPoolExecutor;


	/**
	 * Maps that stores discovered participants. discovered participant is shared with
	 * all entities created by this participant. 
	 */
	private final HashMap<GuidPrefix_t, ParticipantData> discoveredParticipants =  new HashMap<>();
	private final HashMap<GUID_t, ReaderData> discoveredReaders = new HashMap<>();
	private final HashMap<GUID_t, WriterData> discoveredWriters = new HashMap<>();
	@SuppressWarnings("unused")
	private final HashMap<GUID_t, TopicData> discoveredTopics = new HashMap<>();
	
	/**
	 * A map that stores network receivers for each locator we know. (For listening purposes)
	 */
	private Set<UDPReceiver> receivers = new HashSet<UDPReceiver>();

	private final List<RTPSReader<?>> readerEndpoints = new LinkedList<>();
	private final List<RTPSWriter<?>> writerEndpoints = new LinkedList<>();

	private final GUID_t guid;

	private Locator_t meta_mcLoc;
	private Locator_t meta_ucLoc;
	private Locator_t mcLoc;
	private Locator_t ucLoc;

	private final int domainId;
	private final int participantId;

	private final LivelinessManager livelinessManager;

	/**
	 * 
	 */
	public RTPSParticipant(int participantId) {
		this(0, participantId);
	}
	
	/**
	 * Creates a new participant with given domainId and participantId. Domain ID and particiapnt ID
	 * is used to construct unicast locators to this RTPSParticipant. In general, participants in the same
	 * domain get to know each other through SPDP. Each participant has a uniques unicast locator to access
	 * its endpoints. 
	 *  
	 * @param domainId Domain ID of the participant
	 * @param participantId Participant ID 
	 * @see EntityId_t
	 */
	public RTPSParticipant(int domainId, int participantId) {
		this.domainId = domainId;
		this.participantId = participantId; 
		Random r = new Random(System.currentTimeMillis());
		this.guid = new GUID_t(new GuidPrefix_t((byte) domainId, (byte) participantId, r.nextInt()), EntityId_t.PARTICIPANT);

		log.info("Creating participant {} for domain {}", participantId, domainId);

		int corePoolSize = config.getIntProperty("jrtps.thread-pool.core-size", 10);
		int maxPoolSize = config.getIntProperty("jrtps.thread-pool.max-size", 20);
		threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, 5, TimeUnit.SECONDS, 
				new LinkedBlockingQueue<Runnable>(maxPoolSize));

		log.debug("Settings for thread-pool: core-size {}, max-size {}", corePoolSize, maxPoolSize);

		meta_mcLoc = Locator_t.defaultDiscoveryMulticastLocator(domainId);
		meta_ucLoc = Locator_t.defaultMetatrafficUnicastLocator(domainId, participantId);
		mcLoc = Locator_t.defaultUserMulticastLocator(domainId);
		ucLoc = Locator_t.defaultUserUnicastLocator(domainId, participantId);

		// TODO: Consider moving builtin stuff to uDDS project

		// ----  Builtin marshallers  ---------------
		ParticipantDataMarshaller pdm = new ParticipantDataMarshaller();
		WriterDataMarshaller wdm = new WriterDataMarshaller();		
		ReaderDataMarshaller rdm = new ReaderDataMarshaller();
		TopicDataMarshaller tdm = new TopicDataMarshaller();		

		livelinessManager = new LivelinessManager(this);
		
		QualityOfService spdpQoS = new QualityOfService();
		QualityOfService sedpQoS = new QualityOfService();
		try {
			sedpQoS.setPolicy(new QosReliability(QosReliability.Kind.RELIABLE, new Duration_t(0, 0)));
		} catch (InconsistentPolicy e) {
			log.error("Got InconsistentPolicy exception. This is an internal error", e);
		}

		// ----  Create a Writers for SEDP  ---------
		createWriter(EntityId_t.SEDP_BUILTIN_PUBLICATIONS_WRITER, 
				WriterData.BUILTIN_TOPIC_NAME, WriterData.class.getName(), wdm, sedpQoS);
		createWriter(EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER, 
				ReaderData.BUILTIN_TOPIC_NAME, ReaderData.class.getName(), rdm, sedpQoS);
		// NOTE: It is not mandatory to publish TopicData
		// createWriter(EntityId_t.SEDP_BUILTIN_TOPIC_WRITER, TopicData.BUILTIN_TOPIC_NAME, tMarshaller);


		// ----  Create a Reader for SPDP  -----------------------
		RTPSReader<ParticipantData> partReader = createReader(EntityId_t.SPDP_BUILTIN_PARTICIPANT_READER, 
				ParticipantData.BUILTIN_TOPIC_NAME, ParticipantData.class.getName(), pdm, spdpQoS);
		partReader.addListener(new BuiltinParticipantDataListener(this, discoveredParticipants));


		// ----  Create a Readers for SEDP  ---------
		RTPSReader<WriterData> pubReader = createReader(EntityId_t.SEDP_BUILTIN_PUBLICATIONS_READER, 
				WriterData.BUILTIN_TOPIC_NAME, WriterData.class.getName(), wdm, sedpQoS);
		pubReader.addListener(new BuiltinWriterDataListener(this, discoveredWriters));

		RTPSReader<ReaderData> subReader = createReader(EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_READER, 
				ReaderData.BUILTIN_TOPIC_NAME, ReaderData.class.getName(), rdm, sedpQoS);
		subReader.addListener(new BuiltinReaderDataListener(this, discoveredParticipants, discoveredReaders));

		// NOTE: It is not mandatory to publish TopicData, create reader anyway. Maybe someone publishes TopicData.
		RTPSReader<TopicData> topicReader = createReader(EntityId_t.SEDP_BUILTIN_TOPIC_READER, 
				TopicData.BUILTIN_TOPIC_NAME, TopicData.class.getName(), tdm, sedpQoS);
		topicReader.addListener(new BuiltinTopicDataListener(this));

		// ----  Create a Writer for SPDP  -----------------------
		RTPSWriter<ParticipantData> spdp_w = createWriter(EntityId_t.SPDP_BUILTIN_PARTICIPANT_WRITER, 
				ParticipantData.BUILTIN_TOPIC_NAME, ParticipantData.class.getName(), pdm, spdpQoS);

		ParticipantData pd = createSPDPParticipantData();
		spdp_w.createChange(pd);
		
		createSPDPResender(config.getSPDPResendPeriod(), spdp_w);
		
		livelinessManager.start();
		
		participantId++;
	}


	/**
	 * Starts this Participant. All the configured endpoints are initialized.
	 * 
	 * @throws SocketException
	 */
	public void start() throws SocketException {
		// TODO: We should have endpoints for TCP, InMemory, What else? encrypted?, signed? 
		// UDP is required by the specification. 
		// TODO: should we have just one RTPSMessageHandler
		receivers.add(new UDPReceiver(meta_mcLoc, new RTPSMessageHandler(this), config.getBufferSize()));
		receivers.add(new UDPReceiver(meta_ucLoc, new RTPSMessageHandler(this), config.getBufferSize()));
		receivers.add(new UDPReceiver(mcLoc, new RTPSMessageHandler(this), config.getBufferSize()));			
		receivers.add(new UDPReceiver(ucLoc, new RTPSMessageHandler(this), config.getBufferSize()));		

		for (UDPReceiver receiver : receivers) {
			threadPoolExecutor.execute(receiver);
		}
		
		log.debug("{} receivers, {} readers and {} writers started", receivers.size(), readerEndpoints.size(), writerEndpoints.size());
	}
	
	
	/**
	 * Each user entity is assigned a unique number, this field is used for that purpose
	 */
	private volatile int userEntityIdx = 1;

	/**
	 * Asserts liveliness of RTPSWriters, whose QosLiveliness kind is MANUAL_BY_PARTICIPANT.
	 * 
	 * @see net.sf.jrtps.message.parameter.QosLiveliness
	 */
	public void assertLiveliness() {
		livelinessManager.assertLiveliness();
	}
	
	/**
	 * Creates an user defined writer. Topic name is the simple name of Class given.
	 * and type name is the fully qualified class name of the class given. QualityOfService
	 * is default.
	 * 
	 * @param c
	 * @param marshaller
	 * @return RTPSWriter
	 * @see java.lang.Class#getSimpleName()
	 * @see java.lang.Class#getName()
	 */
	public <T> RTPSWriter<T> createWriter(Class<T> c, Marshaller<?> marshaller) {
		return createWriter(c.getSimpleName(), c, c.getName(), marshaller, new QualityOfService());
	}
	
	/**
	 * Creates an user defined entity with given topic and type names.
	 * 
	 * @param topicName
	 * @param type
	 * @param typeName
	 * @param marshaller
	 * @param qos QualityOfService
	 * @return RTPSWriter
	 */
	public <T>RTPSWriter<T> createWriter(String topicName, Class<T> type, String typeName, Marshaller<?> marshaller, QualityOfService qos) {
		int myIdx = userEntityIdx++;
		byte[] myKey = new byte[3];
		myKey[0] = (byte) (myIdx & 0xff);
		myKey[1] = (byte) (myIdx >> 8 & 0xff);
		myKey[2] = (byte) (myIdx >> 16 & 0xff);
		
		int kind = 0x02; // User defined writer, with key, see 9.3.1.2 Mapping of the EntityId_t
		if (!marshaller.hasKey(type)) {
			kind = 0x03; // User defined writer, no key
		}
		
		return createWriter(new EntityId_t.UserDefinedEntityId(myKey, kind), topicName, typeName, marshaller, qos);
	}

	/**
	 * Creates a new RTPSWriter.
	 * 
	 * @param eId
	 * @param topicName
	 * @param typeName
	 * @param marshaller
	 * @param qos
	 * @return RTPSWriter
	 */
	<T> RTPSWriter<T> createWriter(EntityId_t eId, String topicName, String typeName, Marshaller<?> marshaller, QualityOfService qos) {
		RTPSWriter<T> writer = new RTPSWriter<T>(this, eId, topicName, marshaller, qos, config);
		writer.setDiscoveredParticipants(discoveredParticipants);

		writerEndpoints.add(writer);

		@SuppressWarnings("unchecked")
		RTPSWriter<WriterData> pw = (RTPSWriter<WriterData>) getWritersForTopic(WriterData.BUILTIN_TOPIC_NAME).get(0);
		WriterData wd = new WriterData(writer.getTopicName(), typeName, writer.getGuid(), qos);
		pw.createChange(wd);
		pw.notifyReaders();
		
		livelinessManager.registerWriter(writer);
		
		return writer;
	}

	/**
	 * Creates an user defined reader. Topic name is the simple name of Class given.
	 * and type name is the fully qualified class name of the class given.
	 * 
	 * @param c
	 * @param marshaller
	 * @return RTPSReader
	 * @see java.lang.Class#getSimpleName()
	 * @see java.lang.Class#getName()
	 */
	public <T> RTPSReader<T> createReader(Class<T> c, Marshaller<?> marshaller) {
		return createReader(c.getSimpleName(), c, c.getName(), marshaller, new QualityOfService());
	}
	
	/**
	 * Creates an user defined entity with given topic and type names.
	 * 
	 * @param topicName
	 * @param type
	 * @param typeName
	 * @param marshaller
	 * @return RTPSReader
	 */
	public <T> RTPSReader<T> createReader(String topicName, Class<T> type, String typeName, Marshaller<?> marshaller, 
			QualityOfService qos) {
		int myIdx = userEntityIdx++;
		byte[] myKey = new byte[3];
		myKey[0] = (byte) (myIdx & 0xff);
		myKey[1] = (byte) (myIdx >> 8 & 0xff);
		myKey[2] = (byte) (myIdx >> 16 & 0xff);
		
		int kind = 0x07; // User defined reader, with key, see 9.3.1.2 Mapping of the EntityId_t
		if (!marshaller.hasKey(type)) {
			kind = 0x04; // User defined reader, no key
		}
		
		return createReader(new EntityId_t.UserDefinedEntityId(myKey, kind), topicName, typeName, marshaller, qos);
	}

	/**
	 * Close this RTPSParticipant. All the network listeners will be stopped 
	 * and all the history caches of all entities will be cleared.
	 */
	public void close() {
		log.debug("Closing RTPSParticipant {} in domain {}", participantId, domainId);

		threadPoolExecutor.shutdown();
		
//		// First, close network receivers
		for (UDPReceiver r : receivers) {
			r.close();
		}

		// Then entities
		for (RTPSReader<?> r : readerEndpoints) {
			r.close();
		}
		for (RTPSWriter<?> w : writerEndpoints) {
			w.close();
		}
		
		livelinessManager.stop();
		
		try {
			boolean terminated = threadPoolExecutor.awaitTermination(1, TimeUnit.SECONDS);
			if (!terminated) {
				threadPoolExecutor.shutdownNow();
			}
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Gets the guid of this participant.
	 * @return guid
	 */
	public GUID_t getGuid() {
		return guid;
	}

	
	/**
	 * Creates a new RTPSReader.
	 * 
	 * @param eId
	 * @param topicName
	 * @param typeName
	 * @param marshaller
	 * @param qos 
	 * @return RTPSReader
	 */
	<T> RTPSReader<T> createReader(EntityId_t eId, String topicName, String typeName, Marshaller<?> marshaller, 
			QualityOfService qos) {
		RTPSReader<T> reader = new RTPSReader<T>(this, eId, topicName, marshaller, qos, config);
		reader.setDiscoveredParticipants(discoveredParticipants);

		readerEndpoints.add(reader);

		@SuppressWarnings("unchecked")
		RTPSWriter<ReaderData> sw = (RTPSWriter<ReaderData>) getWritersForTopic(ReaderData.BUILTIN_TOPIC_NAME).get(0);
		ReaderData rd = new ReaderData(topicName, typeName, reader.getGuid(), qos);
		sw.createChange(rd);

		return reader;
	}




	List<RTPSWriter<?>> getWritersForTopic(String topicName) {
		List<RTPSWriter<?>> writers = new LinkedList<>();
		for (RTPSWriter<?> w : writerEndpoints) {
			if (w.getTopicName().equals(topicName)) {
				writers.add(w);
			}
		}

		return writers;
	}

	List<RTPSReader<?>> getReadersForTopic(String topicName) {
		List<RTPSReader<?>> readers = new LinkedList<>();
		for (RTPSReader<?> r : readerEndpoints) {
			if (r.getTopicName().equals(topicName)) {
				readers.add(r);
			}
		}

		return readers;
	}



	/**
	 * Finds a Reader with given entity id.
	 * @param readerId
	 * @return RTPSReader
	 */
	private RTPSReader<?> getReader(EntityId_t readerId) {
		for (RTPSReader<?> reader : readerEndpoints) {
			if (reader.getGuid().entityId.equals(readerId)) {
				return reader;
			}
		}

		return null;
	}

	/**
	 * Gets a Reader with given readerId. If readerId is null or EntityId_t.UNKNOWN_ENTITY,
	 * a search is made to match with corresponding writerId. I.e. If writer is
	 * SEDP_BUILTIN_PUBLICATIONS_WRITER, a search is made for SEDP_BUILTIN_PUBLICATIONS_READER.
	 * 
	 * @param readerId
	 * @param writerId
	 * @return RTPSReader
	 */
	RTPSReader<?> getReader(EntityId_t readerId, EntityId_t writerId) {
		if (readerId != null && !EntityId_t.UNKNOWN_ENTITY.equals(readerId)) {
			return getReader(readerId);
		}

		if (writerId.equals(EntityId_t.SEDP_BUILTIN_PUBLICATIONS_WRITER)) {
			return getReader(EntityId_t.SEDP_BUILTIN_PUBLICATIONS_READER);
		}

		if (writerId.equals(EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER)) {
			return getReader(EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_READER);
		}

		if (writerId.equals(EntityId_t.SEDP_BUILTIN_TOPIC_WRITER)) {
			return getReader(EntityId_t.SEDP_BUILTIN_TOPIC_READER);
		}

		if (writerId.equals(EntityId_t.SPDP_BUILTIN_PARTICIPANT_WRITER)) {
			return getReader(EntityId_t.SPDP_BUILTIN_PARTICIPANT_READER);
		}

		log.warn("Failed to find RTPSReader for reader entity {} or matching writer entity {}", readerId, writerId);
		return null;
	}



	/**
	 * Finds a Writer with given entity id.
	 * @param writerId
	 * @return RTPSWriter
	 */
	RTPSWriter<?> getWriter(EntityId_t writerId) {
		for (RTPSWriter<?> writer : writerEndpoints) {
			if (writer.getGuid().entityId.equals(writerId)) {
				return writer;
			}
		}

		return null;
	}


	RTPSWriter<?> getWriter(EntityId_t writerId, EntityId_t readerId) {
		if (writerId != null && !EntityId_t.UNKNOWN_ENTITY.equals(writerId)) {
			return getWriter(writerId);
		}

		if (readerId.equals(EntityId_t.SEDP_BUILTIN_PUBLICATIONS_READER)) {
			return getWriter(EntityId_t.SEDP_BUILTIN_PUBLICATIONS_WRITER);
		}

		if (readerId.equals(EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_READER)) {
			return getWriter(EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER);
		}

		if (readerId.equals(EntityId_t.SEDP_BUILTIN_TOPIC_READER)) {
			return getWriter(EntityId_t.SEDP_BUILTIN_TOPIC_WRITER);
		}

		if (readerId.equals(EntityId_t.SPDP_BUILTIN_PARTICIPANT_READER)) {
			return getWriter(EntityId_t.SPDP_BUILTIN_PARTICIPANT_WRITER);
		}

		log.warn("Failed to find Writer for writer {} or matching reader {}",  writerId, readerId);
		return null;
	}


	private ParticipantData createSPDPParticipantData() {
		int epSet = createEndpointSet();
		ParticipantData pd = new ParticipantData(guid.prefix, epSet, ucLoc,  mcLoc,  meta_ucLoc, meta_mcLoc);

		log.debug("Created ParticipantData: {}", pd);

		return pd;
	}


	private int createEndpointSet() {
		int eps = 0;
		for (RTPSReader<?> r : readerEndpoints) {
			eps |= r.endpointSetId();
		}
		for (RTPSWriter<?> w : writerEndpoints) {
			eps |= w.endpointSetId();
		}

		log.debug("{}", new BuiltinEndpointSet(eps));

		return eps;
	}


	/**
	 * Adds a runnable to be run with this participants thread pool.
	 * @param runnable
	 */
	void addRunnable(Runnable runnable) {
		threadPoolExecutor.execute(runnable);
	}


	private void createSPDPResender(final Duration_t period, final RTPSWriter<ParticipantData> spdp_w) {
		Runnable resendRunnable = new Runnable() {
		    @Override
			public void run() {
				boolean running = true;
				while (running) {
					spdp_w.sendData(null, EntityId_t.SPDP_BUILTIN_PARTICIPANT_READER, 0);
					try {
						running = !threadPoolExecutor.awaitTermination(period.asMillis(), TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						running = false;
					}
				}
			}
		};

		log.debug("[{}] Starting resend thread with period {}", getGuid().entityId, period);		
		threadPoolExecutor.execute(resendRunnable);
	}
}
