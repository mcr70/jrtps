package net.sf.jrtps;

import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.builtin.ParticipantData;
import net.sf.jrtps.builtin.ReaderData;
import net.sf.jrtps.builtin.TopicData;
import net.sf.jrtps.builtin.WriterData;
import net.sf.jrtps.transport.UDPReceiver;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;
import net.sf.jrtps.types.Locator;

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
	private final HashMap<GuidPrefix, ParticipantData> discoveredParticipants =  new HashMap<>();
	private final HashMap<Guid, ReaderData> discoveredReaders = new HashMap<>();
	private final HashMap<Guid, WriterData> discoveredWriters = new HashMap<>();
	@SuppressWarnings("unused")
	private final HashMap<Guid, TopicData> discoveredTopics = new HashMap<>();
	
	/**
	 * A map that stores network receivers for each locator we know. (For listening purposes)
	 */
	private Set<UDPReceiver> receivers = new HashSet<UDPReceiver>();

	private final List<RTPSReader<?>> readerEndpoints = new LinkedList<>();
	private final List<RTPSWriter<?>> writerEndpoints = new LinkedList<>();

	private final Guid guid;

	private Locator meta_mcLoc;
	private Locator meta_ucLoc;
	private Locator mcLoc;
	private Locator ucLoc;

	private final int domainId;
	private final int participantId;
	
	/**
	 * Creates a new participant with given domainId and participantId. Domain ID and particiapnt ID
	 * is used to construct unicast locators to this RTPSParticipant. In general, participants in the same
	 * domain get to know each other through SPDP. Each participant has a uniques unicast locator to access
	 * its endpoints. 
	 *  
	 * @param domainId Domain ID of the participant
	 * @param participantId Participant ID 
	 * @param ucLoc 
	 * @param mcLoc 
	 * @param meta_ucLoc 
	 * @param meta_mcLoc 
	 * @see EntityId
	 */
	public RTPSParticipant(int domainId, int participantId, ThreadPoolExecutor tpe, 
			Locator meta_mcLoc, Locator meta_ucLoc, Locator mcLoc, Locator ucLoc) {
		this.domainId = domainId;
		this.participantId = participantId; 
		this.threadPoolExecutor = tpe;
		
		this.meta_mcLoc = meta_mcLoc;
		this.meta_ucLoc = meta_ucLoc;
		this.mcLoc = mcLoc;
		this.ucLoc = ucLoc;
		
		Random r = new Random(System.currentTimeMillis());
		this.guid = new Guid(new GuidPrefix((byte) domainId, (byte) participantId, r.nextInt()), EntityId.PARTICIPANT);

		log.info("Creating participant {} for domain {}", participantId, domainId);

				
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

		// TODO: should we have more than just one RTPSMessageHandler
		//       It might cause problems during message processing.
		BlockingQueue<byte[]> queue = new LinkedBlockingQueue<>(config.getMessageQueueSize());
		RTPSMessageHandler handler = new RTPSMessageHandler(this, queue);
		
		threadPoolExecutor.execute(handler);
		
		receivers.add(new UDPReceiver(meta_mcLoc, queue, config.getBufferSize()));
		receivers.add(new UDPReceiver(meta_ucLoc, queue, config.getBufferSize()));
		receivers.add(new UDPReceiver(mcLoc, queue, config.getBufferSize()));			
		receivers.add(new UDPReceiver(ucLoc, queue, config.getBufferSize()));		

		for (UDPReceiver receiver : receivers) {
			threadPoolExecutor.execute(receiver);
		}
		
		log.debug("{} receivers, {} readers and {} writers started", receivers.size(), readerEndpoints.size(), writerEndpoints.size());
	}
	
	
	/**
	 * Each user entity is assigned a unique number, this field is used for that purpose
	 */
	private volatile int userEntityIdx = 1;

	private LinkedBlockingQueue<byte[]> queue;

	
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
		
		return createWriter(new EntityId.UserDefinedEntityId(myKey, kind), topicName, typeName, marshaller, qos);
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
	public <T> RTPSWriter<T> createWriter(EntityId eId, String topicName, String typeName, Marshaller<?> marshaller, QualityOfService qos) {
		RTPSWriter<T> writer = new RTPSWriter<T>(this, eId, topicName, marshaller, qos, config);
		writer.setDiscoveredParticipants(discoveredParticipants);

		writerEndpoints.add(writer);

		@SuppressWarnings("unchecked")
		RTPSWriter<WriterData> pw = (RTPSWriter<WriterData>) getWritersForTopic(WriterData.BUILTIN_TOPIC_NAME).get(0);
		WriterData wd = new WriterData(writer.getTopicName(), typeName, writer.getGuid(), qos);
		pw.write(wd);
				
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
	 * Creates an user defined reader with given topic and type names.
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
		
		return createReader(new EntityId.UserDefinedEntityId(myKey, kind), topicName, typeName, marshaller, qos);
	}

	/**
	 * Close this RTPSParticipant. All the network listeners will be stopped 
	 * and all the history caches of all entities will be cleared.
	 */
	public void close() {
		log.debug("Closing RTPSParticipant {} in domain {}", participantId, domainId);
		
		// close network receivers
		for (UDPReceiver r : receivers) {
			r.close();
		}
	}

	/**
	 * Gets the guid of this participant.
	 * @return guid
	 */
	public Guid getGuid() {
		return guid;
	}

	/**
	 * Gets the domainId of this participant;
	 * @return domainId
	 */
	public int getDomainId() {
		return domainId;
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
				log.debug("waitFor(...) was interrupted");
			}
		}
		
		return false;
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
	public <T> RTPSReader<T> createReader(EntityId eId, String topicName, String typeName, Marshaller<?> marshaller, 
			QualityOfService qos) {
		RTPSReader<T> reader = new RTPSReader<T>(this, eId, topicName, marshaller, qos, config);
		reader.setDiscoveredParticipants(discoveredParticipants);

		readerEndpoints.add(reader);

		@SuppressWarnings("unchecked")
		RTPSWriter<ReaderData> sw = (RTPSWriter<ReaderData>) getWritersForTopic(ReaderData.BUILTIN_TOPIC_NAME).get(0);
		ReaderData rd = new ReaderData(topicName, typeName, reader.getGuid(), qos);
		sw.write(rd);

		return reader;
	}




	public List<RTPSWriter<?>> getWritersForTopic(String topicName) {
		List<RTPSWriter<?>> writers = new LinkedList<>();
		for (RTPSWriter<?> w : writerEndpoints) {
			if (w.getTopicName().equals(topicName)) {
				writers.add(w);
			}
		}

		return writers;
	}

	public List<RTPSReader<?>> getReadersForTopic(String topicName) {
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
	public RTPSReader<?> getReader(EntityId readerId) {
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

		log.warn("Failed to find RTPSReader for reader entity {} or matching writer entity {}", readerId, writerId);
		return null;
	}



	/**
	 * Finds a Writer with given entity id.
	 * @param writerId
	 * @return RTPSWriter
	 */
	public RTPSWriter<?> getWriter(EntityId writerId) {
		for (RTPSWriter<?> writer : writerEndpoints) {
			if (writer.getGuid().entityId.equals(writerId)) {
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

		log.warn("Failed to find Writer for writer {} or matching reader {}",  writerId, readerId);
		return null;
	}
}
