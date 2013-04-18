package alt.rtps;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.builtin.ParticipantData;
import alt.rtps.builtin.ParticipantDataMarshaller;
import alt.rtps.builtin.ReaderData;
import alt.rtps.builtin.ReaderDataMarshaller;
import alt.rtps.builtin.TopicData;
import alt.rtps.builtin.TopicDataMarshaller;
import alt.rtps.builtin.WriterData;
import alt.rtps.builtin.WriterDataMarshaller;
import alt.rtps.message.parameter.BuiltinEndpointSet;
import alt.rtps.transport.UDPReceiver;
import alt.rtps.types.Duration_t;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.GUID_t;
import alt.rtps.types.GuidPrefix_t;
import alt.rtps.types.Locator_t;

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

	private static final String BUILTIN_TOPICNAME_PARTICIPANT = "DCPSParticipant";
	private static final String BUILTIN_TOPICNAME_PUBLICATION = "DCPSPublication";
	private static final String BUILTIN_TOPICNAME_SUBSCRIPTION = "DCPSSubscription";
	private static final String BUILTIN_TOPICNAME_TOPIC = "DCPSTopic";

	private int CORE_POOL_SIZE = 10; // TODO: configurable
	private int MAX_POOL_SIZE = 2 * CORE_POOL_SIZE;

	private int builtinDataKey = 0; // TODO: We should do something with this

	/**
	 * Maps that stores discovered participants. discovered participant is shared with
	 * all entities created by this participant. 
	 */
	private final HashMap<GuidPrefix_t, ParticipantData> discoveredParticipants =  new HashMap<>();


	/**
	 * A map that stores network receivers for each locator we know. (For listening purposes)
	 */
	private Set<UDPReceiver> receivers = new HashSet<UDPReceiver>();

	private ThreadPoolExecutor threadPoolExecutor = 
			new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, 5, TimeUnit.SECONDS, 
					new LinkedBlockingQueue<Runnable>(50)); // TODO: check parameterization


	protected int domainId = 0; // Default domainId
	private static byte participantId = 0; // TODO: We need an instance variable in code below

	private List<RTPSReader> readerEndpoints = new LinkedList<>();
	private List<RTPSWriter> writerEndpoints = new LinkedList<>();

	GUID_t guid;

	private Locator_t meta_mcLoc;
	private Locator_t meta_ucLoc;
	private Locator_t mcLoc;
	private Locator_t ucLoc;

	/**
	 * Creates a new participant with given domainId.
	 *  
	 * @param domainId Domain ID of the participant
	 * @see EntityId_t
	 */
	public RTPSParticipant(int domainId) {
		Random r = new Random(System.currentTimeMillis());
		
		this.guid = new GUID_t(new GuidPrefix_t((byte) domainId, participantId++, r.nextInt()), EntityId_t.PARTICIPANT);

		log.info("Creating participant {} for domain {}", participantId, domainId);
		this.domainId = domainId;

		meta_mcLoc = Locator_t.defaultDiscoveryMulticastLocator(domainId);
		meta_ucLoc = Locator_t.defaultMetatrafficUnicastLocator(domainId, participantId);
		mcLoc = Locator_t.defaultUserMulticastLocator(domainId);
		ucLoc = Locator_t.defaultUserUnicastLocator(domainId, participantId);

		// TODO: Consider moving builtin stuff to uDDS project

		BuiltinListener builtinListener = new BuiltinListener(this, discoveredParticipants);

		// ----  Builtin marshallers  ---------------
		ParticipantDataMarshaller pdm = new ParticipantDataMarshaller();
		WriterDataMarshaller wdm = new WriterDataMarshaller();		
		ReaderDataMarshaller rdm = new ReaderDataMarshaller();
		TopicDataMarshaller tdm = new TopicDataMarshaller();


		// ----  Create a Writers for SEDP  ---------
		RTPSWriter pw = createWriter(EntityId_t.SEDP_BUILTIN_PUBLICATIONS_WRITER, 
				BUILTIN_TOPICNAME_PUBLICATION, WriterData.class.getName(), wdm);
		RTPSWriter sw = createWriter(EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER, 
				BUILTIN_TOPICNAME_SUBSCRIPTION, ReaderData.class.getName(), rdm);
		// createWriter(EntityId_t.SEDP_BUILTIN_TOPIC_WRITER, "DCPSTopic", tMarshaller);


		// ----  Create a Reader for SPDP  -----------------------
		RTPSReader partReader = createReader(EntityId_t.SPDP_BUILTIN_PARTICIPANT_READER, 
				BUILTIN_TOPICNAME_PARTICIPANT, ParticipantData.class.getName(), pdm);
		partReader.addListener(builtinListener);


		// ----  Create a Readers for SEDP  ---------
		RTPSReader pubReader = createReader(EntityId_t.SEDP_BUILTIN_PUBLICATIONS_READER, 
				BUILTIN_TOPICNAME_PUBLICATION, WriterData.class.getName(), wdm);
		pubReader.addListener(builtinListener);

		RTPSReader subReader = createReader(EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_READER, 
				BUILTIN_TOPICNAME_SUBSCRIPTION, ReaderData.class.getName(),rdm);
		subReader.addListener(builtinListener);

		RTPSReader topicReader = createReader(EntityId_t.SEDP_BUILTIN_TOPIC_READER, 
				BUILTIN_TOPICNAME_TOPIC, TopicData.class.getName(), tdm);
		topicReader.addListener(builtinListener);

		// ----  Create a Writer for SPDP  -----------------------
		RTPSWriter spdp_w = createWriter(EntityId_t.SPDP_BUILTIN_PARTICIPANT_WRITER, 
				BUILTIN_TOPICNAME_PARTICIPANT, ParticipantData.class.getName(), pdm);

		ParticipantData pd = createSPDPParticipantData();
		spdp_w.createChange(pd);
		spdp_w.setResendDataPeriod(new Duration_t(10, 0), EntityId_t.SPDP_BUILTIN_PARTICIPANT_READER); // Starts a resender thread
		//spdp_w.addMatchedEndpointLocator(Locator_t.defaultDiscoveryMulticastLocator(domainId));

		participantId++;
	}



	public void start() throws SocketException {

		receivers.add(new UDPReceiver(meta_mcLoc, this));
		receivers.add(new UDPReceiver(meta_ucLoc, this));
		receivers.add(new UDPReceiver(mcLoc, this));			
		receivers.add(new UDPReceiver(ucLoc, this));		

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
	 * Creates an user defined writer. Topic name is the simple name of Class given.
	 * and type name is the fully qualified class name of the class given.
	 * 
	 * @param c
	 * @param marshaller
	 * @return
	 * @see java.lang.Class.getSimpleName()
	 * @see java.lang.Class.getName()
	 */
	public RTPSWriter createWriter(Class c, Marshaller marshaller) {
		return createWriter(c.getSimpleName(), c.getName(), marshaller);
	}
	
	/**
	 * Creates an user defined entity with given topic and type names.
	 * 
	 * @param topicName
	 * @param typeName
	 * @param marshaller
	 * @return
	 */
	public RTPSWriter createWriter(String topicName, String typeName, Marshaller<?> marshaller) {
		int myIdx = userEntityIdx++;
		byte[] myKey = new byte[3];
		myKey[0] = (byte) (myIdx & 0xff);
		myKey[1] = (byte) (myIdx >> 8 & 0xff);
		myKey[2] = (byte) (myIdx >> 16 & 0xff);
		
		int kind = 0x02; // User defined writer, with key
		return createWriter(new EntityId_t.UserDefinedEntityId(myKey, kind), topicName, typeName, marshaller);
	}

	/**
	 * Creates a new RTPSWriter.
	 * 
	 * @param eId
	 * @param topicName
	 * @param marshaller
	 * @return
	 */
	private RTPSWriter createWriter(EntityId_t eId, String topicName, String typeName, Marshaller marshaller) {
		RTPSWriter writer = new RTPSWriter(guid.prefix, eId, topicName, marshaller);
		writer.setDiscoveredParticipants(discoveredParticipants);

		writerEndpoints.add(writer);

		RTPSWriter pw = getWriterForTopic(BUILTIN_TOPICNAME_PUBLICATION);
		WriterData wd = new WriterData(writer.getTopicName(), typeName, writer.getGuid());
		pw.createChange(wd);

		return writer;
	}

	/**
	 * Creates an user defined reader. Topic name is the simple name of Class given.
	 * and type name is the fully qualified class name of the class given.
	 * 
	 * @param c
	 * @param marshaller
	 * @return
	 * @see java.lang.Class.getSimpleName()
	 * @see java.lang.Class.getName()
	 */
	public RTPSReader createReader(Class c, Marshaller marshaller) {
		return createReader(c.getSimpleName(), c.getName(), marshaller);
	}
	
	/**
	 * Creates an user defined entity with given topic and type names.
	 * 
	 * @param topicName
	 * @param typeName
	 * @param marshaller
	 * @return
	 */
	public RTPSReader createReader(String topicName, String typeName, Marshaller<?> marshaller) {
		int myIdx = userEntityIdx++;
		byte[] myKey = new byte[3];
		myKey[0] = (byte) (myIdx & 0xff);
		myKey[1] = (byte) (myIdx >> 8 & 0xff);
		myKey[2] = (byte) (myIdx >> 16 & 0xff);
		
		int kind = 0x07; // User defined reader, with key
		return createReader(new EntityId_t.UserDefinedEntityId(myKey, kind), topicName, typeName, marshaller);
	}

	
	/**
	 * Creates a new RTPSReader.
	 * 
	 * @param eId
	 * @param topicName
	 * @param typeName
	 * @param marshaller
	 * @return
	 */
	private RTPSReader createReader(EntityId_t eId, String topicName, String typeName, Marshaller marshaller) {
		RTPSReader reader = new RTPSReader(guid.prefix, eId, topicName, marshaller);
		reader.setDiscoveredParticipants(discoveredParticipants);

		readerEndpoints.add(reader);

		RTPSWriter sw = getWriterForTopic(BUILTIN_TOPICNAME_SUBSCRIPTION);
		ReaderData rd = new ReaderData(topicName, typeName, reader.getGuid());
		sw.createChange(rd);

		return reader;
	}




	RTPSWriter getWriterForTopic(String topicName) {
		for (RTPSWriter w : writerEndpoints) {
			if (w.getTopicName().equals(topicName)) {
				return w;
			}
		}

		return null;
	}

	RTPSReader getReaderForTopic(String topicName) {
		for (RTPSReader r : readerEndpoints) {
			if (r.getTopicName().equals(topicName)) {
				return r;
			}
		}

		return null;
	}



	/**
	 * Finds a Reader with given entity id.
	 * @param readerId
	 * @return 
	 */
	public RTPSReader getReader(EntityId_t readerId) {
		for (RTPSReader reader : readerEndpoints) {
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
	 * @return
	 */
	public RTPSReader getReader(EntityId_t readerId, EntityId_t writerId) {
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
	 * @return 
	 */
	public RTPSWriter getWriter(EntityId_t writerId) {
		for (RTPSWriter writer : writerEndpoints) {
			if (writer.getGuid().entityId.equals(writerId)) {
				return writer;
			}
		}

		return null;
	}


	public RTPSWriter getWriter(EntityId_t writerId, EntityId_t readerId) {
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
		for (RTPSReader r : readerEndpoints) {
			eps |= r.endpointSetId();
		}
		for (RTPSWriter w : writerEndpoints) {
			eps |= w.endpointSetId();
		}

		log.debug("{}", new BuiltinEndpointSet(eps));

		return eps;
	}



	public RTPSReader getMatchingReader(EntityId_t writerId) {
		if (writerId.isBuiltinEntity()) { // We can find matching writer only for builtin stuff
			if (writerId.equals(EntityId_t.SPDP_BUILTIN_PARTICIPANT_WRITER)) {
				return getReader(EntityId_t.SPDP_BUILTIN_PARTICIPANT_READER);
			}
			else if (writerId.equals(EntityId_t.SEDP_BUILTIN_PUBLICATIONS_WRITER)) {
				return getReader(EntityId_t.SEDP_BUILTIN_PUBLICATIONS_READER);
			}
			else if (writerId.equals(EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER)) {
				return getReader(EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_READER);
			}
			else if (writerId.equals(EntityId_t.SEDP_BUILTIN_TOPIC_WRITER)) {
				return getReader(EntityId_t.SEDP_BUILTIN_TOPIC_READER);
			}
		}

		return null;
	}


	/**
	 * Close this RTPSParticipant. All the network listeners will be stopped 
	 * and all the history caches of all entities will be cleared.
	 */
	public void close() {
		// First, close network receivers
		for (UDPReceiver r : receivers) {
			r.close();
		}

		// Then entities
		for (RTPSReader r : readerEndpoints) {
			r.close();
		}
		for (RTPSWriter w : writerEndpoints) {
			w.close();
		}
	}
}
