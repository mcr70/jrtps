package alt.rtps;

import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
import alt.rtps.builtin.TopicDataMarshaller;
import alt.rtps.builtin.WriterData;
import alt.rtps.builtin.WriterDataMarshaller;
import alt.rtps.message.parameter.BuiltinEndpointSet;
import alt.rtps.transport.Marshaller;
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
		this.guid = new GUID_t(new GuidPrefix_t((byte) domainId, participantId++), EntityId_t.PARTICIPANT);

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
		RTPSWriter pw = createWriter(EntityId_t.SEDP_BUILTIN_PUBLICATIONS_WRITER, BUILTIN_TOPICNAME_PUBLICATION, wdm);
		RTPSWriter sw = createWriter(EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_WRITER, BUILTIN_TOPICNAME_SUBSCRIPTION, rdm);
		// createWriter(EntityId_t.SEDP_BUILTIN_TOPIC_WRITER, "DCPSTopic", tMarshaller);


		// ----  Create a Reader for SPDP  -----------------------
		RTPSReader partReader = createReader(EntityId_t.SPDP_BUILTIN_PARTICIPANT_READER, BUILTIN_TOPICNAME_PARTICIPANT, pdm);
		partReader.addListener(builtinListener);


		// ----  Create a Readers for SEDP  ---------
		RTPSReader pubReader = createReader(EntityId_t.SEDP_BUILTIN_PUBLICATIONS_READER, BUILTIN_TOPICNAME_PUBLICATION, wdm);
		pubReader.addListener(builtinListener);

		RTPSReader subReader = createReader(EntityId_t.SEDP_BUILTIN_SUBSCRIPTIONS_READER, BUILTIN_TOPICNAME_SUBSCRIPTION, rdm);
		subReader.addListener(builtinListener);

		RTPSReader topicReader = createReader(EntityId_t.SEDP_BUILTIN_TOPIC_READER, BUILTIN_TOPICNAME_TOPIC, tdm);
		topicReader.addListener(builtinListener);

		// ----  Create a Writer for SPDP  -----------------------
		RTPSWriter spdp_w = createWriter(EntityId_t.SPDP_BUILTIN_PARTICIPANT_WRITER, BUILTIN_TOPICNAME_PARTICIPANT, pdm);

		ParticipantData pd = createSPDPParticipantData();
		spdp_w.getHistoryCache().createChange(pd, 1);
		spdp_w.setResendDataPeriod(new Duration_t(10, 0)); // Starts a resender thread
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
	 * Creates a new RTPSWriter.
	 * 
	 * @param eId
	 * @param topicName
	 * @param marshaller
	 * @return
	 */
	public RTPSWriter createWriter(EntityId_t eId, String topicName, Marshaller marshaller) {
		RTPSWriter writer = new RTPSWriter(guid.prefix, eId, topicName, marshaller);
		writer.setDiscoveredParticipants(discoveredParticipants);

		writerEndpoints.add(writer);

		RTPSWriter pw = getWriterForTopic(BUILTIN_TOPICNAME_PUBLICATION);
		WriterData wd = new WriterData(writer.getTopicName(), WriterData.class.getName(), pw.getGuid());
		pw.getHistoryCache().createChange(wd);

		return writer;
	}

	/**
	 * Creates a new RTPSReader.
	 * 
	 * @param eId
	 * @param topicName
	 * @param marshaller
	 * @return
	 */
	public RTPSReader createReader(EntityId_t eId, String topicName, Marshaller marshaller) {
		RTPSReader reader = new RTPSReader(guid.prefix, eId, topicName, marshaller);
		reader.setDiscoveredParticipants(discoveredParticipants);

		readerEndpoints.add(reader);

		RTPSWriter sw = getWriterForTopic(BUILTIN_TOPICNAME_SUBSCRIPTION);
		ReaderData rd = new ReaderData(reader.getTopicName(), ReaderData.class.getName(), sw.getGuid());
		sw.getHistoryCache().createChange(rd);

		return reader;
	}
	public RTPSReader createReader(EntityId_t eId, String topicName, String typeName, Marshaller marshaller) {
		RTPSReader reader = new RTPSReader(guid.prefix, eId, topicName, marshaller);
		reader.setDiscoveredParticipants(discoveredParticipants);

		readerEndpoints.add(reader);

		RTPSWriter sw = getWriterForTopic(BUILTIN_TOPICNAME_SUBSCRIPTION);
		ReaderData rd = new ReaderData(topicName, typeName, sw.getGuid());
		sw.getHistoryCache().createChange(rd);

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

	Endpoint getReaderForTopic(String topicName) {
		for (Endpoint r : readerEndpoints) {
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

		//log.debug("{}", new BuiltinEndpointSet(eps));

		//System.out.println("EPS: " + new BuiltinEndpointSet(0x3cf));
		//System.out.println("EPS: " + new BuiltinEndpointSet(0x415));
		//eps = 0x0; // 0x3cf, 0x415

		// Endpointset: Readers
		//eps |= BuiltinEndpointSet.DISC_BUILTIN_ENDPOINT_PARTICIPANT_DETECTOR;
		//eps |= BuiltinEndpointSet.DISC_BUILTIN_ENDPOINT_PUBLICATION_DETECTOR;
		//eps |= BuiltinEndpointSet.DISC_BUILTIN_ENDPOINT_SUBSCRIPTION_DETECTOR;

		// Endpointset: Writers
		//eps |= BuiltinEndpointSet.DISC_BUILTIN_ENDPOINT_PARTICIPANT_ANNOUNCER;
		//eps |= BuiltinEndpointSet.DISC_BUILTIN_ENDPOINT_PUBLICATION_ANNOUNCER;
		//eps |= BuiltinEndpointSet.DISC_BUILTIN_ENDPOINT_SUBSCRIPTION_ANNOUNCER;

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
}
