package net.sf.jrtps;

import java.net.SocketException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.jrtps.builtin.ParticipantData;
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
	private final Map<GuidPrefix, ParticipantData> discoveredParticipants;
	
	/**
	 * A Set that stores network receivers for each locator we know. (For listening purposes)
	 */
	private Set<UDPReceiver> receivers = new HashSet<UDPReceiver>();

	private final List<RTPSReader<?>> readerEndpoints = new LinkedList<>();
	private final List<RTPSWriter<?>> writerEndpoints = new LinkedList<>();

	private final Guid guid;

	/**
	 * Locators of this RTPSParticipant
	 */
	private final Set<Locator> locators;

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
	 * @param locators a Set of Locators
	 * 
	 * @see EntityId
	 */
	public RTPSParticipant(int domainId, int participantId, ThreadPoolExecutor tpe, Set<Locator> locators, 
			Map<GuidPrefix, ParticipantData> discoveredParticipants) { 
		this.domainId = domainId;
		this.participantId = participantId; 
		this.threadPoolExecutor = tpe;
		this.locators = locators;
		this.discoveredParticipants = discoveredParticipants;
		
		Random r = new Random(System.currentTimeMillis());
		int vmid = r.nextInt();
		byte[] prefix = new byte[] {(byte) domainId, (byte) participantId, (byte) (vmid>>8 & 0xff), 
				(byte) (vmid&0xff), 0xc,0xa,0xf,0xe,0xb,0xa,0xb,0xe};
		
		this.guid = new Guid(new GuidPrefix(prefix), EntityId.PARTICIPANT);

		log.info("Creating participant {} for domain {}", participantId, domainId);
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
		
		int bufferSize = config.getBufferSize();
		for (Locator loc : locators) {
			UDPReceiver receiver = new UDPReceiver(loc, queue, bufferSize);
			receivers.add(receiver);
			threadPoolExecutor.execute(receiver);
		}
		
		log.debug("{} receivers, {} readers and {} writers started", receivers.size(), readerEndpoints.size(), writerEndpoints.size());
	}
	
	
	/**
	 * Creates a new RTPSReader.
	 * 
	 * @param eId EntityId of the reader
	 * @param topicName Name of the topic
	 * @param marshaller
	 * @param qos QualityOfService
	 * 
	 * @return RTPSReader
	 */
	public <T> RTPSReader<T> createReader(EntityId eId, String topicName, Marshaller<?> marshaller, 
			QualityOfService qos) {
		RTPSReader<T> reader = new RTPSReader<T>(this, eId, topicName, marshaller, qos, config);
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
	 *
	 * @return RTPSWriter
	 */
	public <T> RTPSWriter<T> createWriter(EntityId eId, String topicName, WriterCache wCache, QualityOfService qos) {
		RTPSWriter<T> writer = new RTPSWriter<T>(this, eId, topicName, wCache, qos, config);
		writer.setDiscoveredParticipants(discoveredParticipants);

		writerEndpoints.add(writer);

		return writer;
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
	 * Finds a Reader with given entity id.
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

		log.warn("Failed to find Writer for writer {} or matching reader {}",  writerId, readerId);
		return null;
	}
}
