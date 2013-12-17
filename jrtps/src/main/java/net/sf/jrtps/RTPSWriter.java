package net.sf.jrtps;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;

import net.sf.jrtps.builtin.ReaderData;
import net.sf.jrtps.message.AckNack;
import net.sf.jrtps.message.Data;
import net.sf.jrtps.message.Heartbeat;
import net.sf.jrtps.message.InfoTimestamp;
import net.sf.jrtps.message.Message;
import net.sf.jrtps.message.data.DataEncapsulation;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.StatusInfo;
import net.sf.jrtps.types.EntityId_t;
import net.sf.jrtps.types.GUID_t;
import net.sf.jrtps.types.GuidPrefix_t;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RTPSWriter implements RTPS writer endpoint.
 * 
 * @author mcr70
 *
 */
public class RTPSWriter<T> extends Endpoint {
	private static final Logger log = LoggerFactory.getLogger(RTPSWriter.class);
	private MessageDigest md5 = null;

	private HashMap<GUID_t, ReaderProxy> matchedReaders = new HashMap<>();

	@SuppressWarnings("rawtypes")
	private final Marshaller marshaller;
	private final HistoryCache<T> writer_cache;
	private final int nackResponseDelay;
	private int heartbeatPeriod;

	private int hbCount; // heartbeat counter. incremented each time hb is sent


	RTPSWriter(RTPSParticipant participant, EntityId_t entityId, String topicName, Marshaller<?> marshaller, 
			QualityOfService qos, Configuration configuration) {
		super(participant, entityId, topicName, qos, configuration);

		this.writer_cache = new HistoryCache(marshaller, qos, this);
		this.marshaller = marshaller;
		this.nackResponseDelay = configuration.getNackResponseDelay(); 
		this.heartbeatPeriod = configuration.getHeartbeatPeriod(); 

		try {
			this.md5 = MessageDigest.getInstance("MD5");
		} 
		catch (NoSuchAlgorithmException e) {
			// Just warn. Actual usage might not even need it.
			log.warn("There is no MD5 algorithm available", e);
		}
	}


	/**
	 * Get the BuiltinEndpointSet ID of this RTPSWriter.
	 * 
	 * @return 0, if this RTPSWriter is not builtin endpoint
	 */
	public int endpointSetId() {
		return getGuid().entityId.getEndpointSetId();
	}


	/**
	 * Notify every matched RTPSReader. 
	 * For reliable readers, a Heartbeat is sent. For best effort readers Data is sent.
	 * This provides means to create multiple changes, before announcing the state to readers.
	 */
	public void notifyReaders() {		
		if (matchedReaders.size() > 0) {
			log.debug("[{}] Notifying {} matched readers of changes in history cache", getGuid().entityId, matchedReaders.size());

			for (ReaderProxy proxy : matchedReaders.values()) {
				GUID_t guid = proxy.getReaderData().getKey();
				// TODO: 8.4.2.2.3 Writers must send periodic HEARTBEAT Messages (reliable only)
				if (proxy.isReliable()) {
					sendHeartbeat(guid.prefix, guid.entityId);
				}
				else {
					sendData(guid.prefix, guid.entityId, proxy.getReadersHighestSeqNum());
					proxy.setReadersHighestSeqNum(writer_cache.getSeqNumMax());
				}
			}
		}
	}

	/**
	 * Assert liveliness of this writer. Matched readers are notified via
	 * Heartbeat message of the liveliness of this writer.
	 */
	public void assertLiveliness() {
		for (GUID_t guid : matchedReaders.keySet()) {
			sendHeartbeat(guid.prefix, guid.entityId, true); // Send Heartbeat regardless of readers QosReliability
		}
	}

	/**
	 * Close this writer. Closing a writer clears its cache of changes.
	 */
	public void close() {
		heartbeatPeriod = 0; // Stops heartbeat thread gracefully 
		matchedReaders.clear();
		writer_cache.clear();
	}


	public void removeMatchedReader(ReaderData readerData) {
		log.info("[{}] Removing matchedReader {}", getGuid().entityId, readerData);
		matchedReaders.remove(readerData);
	}

	public void addMatchedReader(ReaderData readerData) {
		log.info("[{}] Adding matchedReader {}", getGuid().entityId, readerData);

		ReaderProxy proxy = new ReaderProxy(readerData);
		matchedReaders.put(readerData.getKey(), proxy);

		QosDurability readerDurability = 
				(QosDurability) readerData.getQualityOfService().getPolicy(QosDurability.class);

		if (QosDurability.Kind.VOLATILE == readerDurability.getKind()) {
			// VOLATILE readers are marked having received all the samples so far
			log.debug("[{}] Setting highest seqNum to {} for VOLATILE reader", getGuid().entityId, 
					writer_cache.getSeqNumMax());

			proxy.setReadersHighestSeqNum(writer_cache.getSeqNumMax());
		}
		else { 
			// Otherwise, send either HB, or our history
			GUID_t guid = readerData.getKey();

			if (proxy.isReliable()) {
				sendHeartbeat(guid.prefix, guid.entityId);
			}
			else {
				sendData(guid.prefix, guid.entityId, proxy.getReadersHighestSeqNum());
				proxy.setReadersHighestSeqNum(writer_cache.getSeqNumMax());
			}
		}
	}

	/**
	 * Handle incoming AckNack message.
	 * 
	 * @param senderPrefix
	 * @param ackNack
	 */
	void onAckNack(GuidPrefix_t senderPrefix, AckNack ackNack) {
		log.debug("[{}] Got AckNack: {}", getGuid().entityId, ackNack.getReaderSNState());

		ReaderProxy proxy = matchedReaders.get(new GUID_t(senderPrefix, ackNack.getReaderId()));
		if (proxy != null) {
			proxy.ackNackReceived(); // Marks reader as being alive
		} // Note: proxy could be null

		log.debug("[{}] Wait for nack response delay: {} ms", getGuid().entityId, nackResponseDelay);
		getParticipant().waitFor(nackResponseDelay);

		sendData(senderPrefix, ackNack.getReaderId(), ackNack.getReaderSNState().getBitmapBase() - 1);
	}


	/**
	 * Send data to given participant & reader. readersHighestSeqNum specifies
	 * which is the first data to be sent.
	 * 
	 * @param targetPrefix
	 * @param readerId
	 * @param readersHighestSeqNum
	 */
	public void sendData(GuidPrefix_t targetPrefix, EntityId_t readerId, long readersHighestSeqNum) {
		Message m = new Message(getGuid().prefix);
		SortedSet<CacheChange> changes = writer_cache.getChangesSince(readersHighestSeqNum);

		if (changes.size() == 0) {
			log.debug("[{}] sendData() called, but history cache is empty. returning.", getGuid().entityId);
			return;
		}

		long lastSeqNum = 0;
		long firstSeqNum = 0;
		long prevTimeStamp = 0;

		for (CacheChange cc : changes) {			
			try {
				lastSeqNum = cc.getSequenceNumber();

				if (lastSeqNum >= readersHighestSeqNum) {
					long timeStamp = cc.getTimeStamp();
					if (timeStamp > prevTimeStamp) {
						InfoTimestamp infoTS = new InfoTimestamp(timeStamp);
						m.addSubMessage(infoTS);
					}
					prevTimeStamp = timeStamp;

					if (firstSeqNum == 0) {
						firstSeqNum = lastSeqNum;
					}

					log.trace("Marshalling {}", cc.getData());
					Data data = createData(readerId, cc);
					m.addSubMessage(data);
				}
			}
			catch(IOException ioe) {
				log.warn("[{}] Failed to add cache change to message", getGuid().entityId, ioe);
			}
		}

		log.debug("[{}] Sending Data: {}-{}", getGuid().entityId, firstSeqNum, lastSeqNum);
		boolean overFlowed = sendMessage(m, targetPrefix); 
		if (overFlowed) {
			sendHeartbeat(targetPrefix, readerId);
		}
	}

	private void sendHeartbeat(GuidPrefix_t senderPrefix, EntityId_t readerId) {
		sendHeartbeat(senderPrefix, readerId, false);
	}

	private void sendHeartbeat(GuidPrefix_t targetPrefix, EntityId_t readerId, boolean livelinessFlag) {
		Message m = new Message(getGuid().prefix);
		Heartbeat hb = createHeartbeat(readerId);
		hb.livelinessFlag(livelinessFlag);
		m.addSubMessage(hb);

		log.debug("[{}] Sending Heartbeat: {}-{}", getGuid().entityId, hb.getFirstSequenceNumber(), hb.getLastSequenceNumber());
		sendMessage(m, targetPrefix);

		if (!livelinessFlag) {
			ReaderProxy proxy = matchedReaders.get(new GUID_t(targetPrefix, readerId));
			proxy.heartbeatSent();
		}		
	}

	private Heartbeat createHeartbeat(EntityId_t entityId) {
		if (entityId == null) {
			entityId = EntityId_t.UNKNOWN_ENTITY;
		}

		Heartbeat hb = new Heartbeat(entityId, getGuid().entityId,
				writer_cache.getSeqNumMin(), writer_cache.getSeqNumMax(), hbCount++ );

		return hb;
	}



	@SuppressWarnings("unchecked")
	private Data createData(EntityId_t readerId, CacheChange cc) throws IOException {		
		DataEncapsulation dEnc = marshaller.marshall(cc.getData());
		ParameterList inlineQos = new ParameterList();

		if (marshaller.hasKey(cc.getData().getClass())) { // Add KeyHash if present
			byte[] key = marshaller.extractKey(cc.getData());
			if (key == null) {
				key = new byte[0];
			}

			byte[] bytes = null;
			if (key.length < 16) {			
				bytes = new byte[16];
				System.arraycopy(key, 0, bytes, 0, key.length);
			}
			else {
				bytes = md5.digest(key);
			}

			inlineQos.add(new KeyHash(bytes));
		}

		if (!cc.getKind().equals(ChangeKind.WRITE)) { // Add status info for operations other than WRITE
			inlineQos.add(new StatusInfo(cc.getKind()));
		}

		Data data = new Data(readerId, getGuid().entityId, cc.getSequenceNumber(), inlineQos, dEnc);

		return data;
	}


	/**
	 * Checks, if a given change number has been acknowledged by every known
	 * matched reader.
	 * 
	 * @param sequenceNumber sequenceNumber of a change to check
	 * @return true, if every matched reader has acknowledged given change number
	 */
	boolean isAcknowledgedByAll(long sequenceNumber) {
		for (ReaderProxy proxy : matchedReaders.values()) {
			if (proxy.isActive() && proxy.getReadersHighestSeqNum() < sequenceNumber) {
				return false;
			}
		}

		return true;
	}


	public void write(T obj) {
		LinkedList<T> ll = new LinkedList<>();
		ll.add(obj);
		write(ll);
	}
	public void write(List<T> objs) {
		writer_cache.write(objs);	
	}
	public void dispose(List<T> objs) {
		writer_cache.dispose(objs);	
	}
	public void unregister(List<T> objs) {
		writer_cache.unregister(objs);	
	}

}
