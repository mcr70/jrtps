package net.sf.jrtps;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

import net.sf.jrtps.builtin.SubscriptionData;
import net.sf.jrtps.message.AckNack;
import net.sf.jrtps.message.Data;
import net.sf.jrtps.message.Heartbeat;
import net.sf.jrtps.message.InfoTimestamp;
import net.sf.jrtps.message.Message;
import net.sf.jrtps.message.data.DataEncapsulation;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.QosDurability;
import net.sf.jrtps.message.parameter.StatusInfo;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.Guid;
import net.sf.jrtps.types.GuidPrefix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RTPSWriter implements RTPS writer endpoint. 
 * RTPSWriter will not communicate with unknown readers. It is expected that DDS implementation 
 * explicitly call addMatchedReader(ReaderData) and removeMatchedReader(ReaderData).
 * 
 * Samples are written through an implementation of WriterCache, which will be given when creating 
 * RTPSWriter with RTPSParticipant. When RTPSWriter needs to write samples to RTPSReader, it will query
 * WriterCache for the CacheChanges.  
 * 
 * @see WriterCache
 * @see RTPSParticipant#createWriter(EntityId, String, WriterCache, QualityOfService)
 * 
 * @author mcr70
 */
public class RTPSWriter<T> extends Endpoint {
	private static final Logger log = LoggerFactory.getLogger(RTPSWriter.class);

	private Map<Guid, ReaderProxy> readerProxies = new ConcurrentHashMap<>();

	private final WriterCache writer_cache;
	private final int nackResponseDelay;
	private int heartbeatPeriod;

	private int hbCount; // heartbeat counter. incremented each time hb is sent

	private ScheduledFuture<?> announceThread;


	RTPSWriter(RTPSParticipant participant, EntityId entityId, String topicName, WriterCache wCache, 
			QualityOfService qos, Configuration configuration) {
		super(participant, entityId, topicName, qos, configuration);

		this.writer_cache = wCache;
		this.nackResponseDelay = configuration.getNackResponseDelay(); 
		this.heartbeatPeriod = configuration.getHeartbeatPeriod();
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				log.debug("[{}] Starting periodical notification", getGuid().getEntityId());
				try {
					notifyReaders();
				}
				catch(Exception e) {
					log.error("Got exception while doing periodical notification", e);
				}
			}
		};
		
		announceThread = participant.scheduleAtFixedRate(r, heartbeatPeriod);
	}


	/**
	 * Get the BuiltinEndpointSet ID of this RTPSWriter.
	 * 
	 * @return 0, if this RTPSWriter is not builtin endpoint
	 */
	public int endpointSetId() {
		return getGuid().getEntityId().getEndpointSetId();
	}


	/**
	 * Notify every matched RTPSReader. 
	 * For reliable readers, a Heartbeat is sent. For best effort readers Data is sent.
	 * This provides means to create multiple changes, before announcing the state to readers.
	 */
	public void notifyReaders() {		
		if (readerProxies.size() > 0) {
			log.trace("[{}] Notifying {} matched readers of changes in history cache", getGuid().getEntityId(), readerProxies.size());

			for (ReaderProxy proxy : readerProxies.values()) {
				Guid guid = proxy.getSubscriptionData().getKey();
				notifyReader(guid);
			}
		}
	}


	public void notifyReader(Guid guid) {
		ReaderProxy proxy = readerProxies.get(guid);

		// TODO: 8.4.2.2.3 Writers must send periodic HEARTBEAT Messages (reliable only)
		if (proxy != null && proxy.isReliable()) {
			sendHeartbeat(guid.getPrefix(), guid.getEntityId());
		}
		else {
			long readersHighestSeqNum = 0;
			if (proxy != null) { // Might be null, if destination is GuidPrefix.UNKNOWN (SPDP)
				readersHighestSeqNum = proxy.getReadersHighestSeqNum();
			}

			sendData(guid.getPrefix(), guid.getEntityId(), readersHighestSeqNum);

			if (proxy != null) {
				proxy.setReadersHighestSeqNum(writer_cache.getSeqNumMax());
			}
		}
	}


	/**
	 * Assert liveliness of this writer. Matched readers are notified via
	 * Heartbeat message of the liveliness of this writer.
	 */
	public void assertLiveliness() {
		for (Guid guid : readerProxies.keySet()) {
			sendHeartbeat(guid.getPrefix(), guid.getEntityId(), true); // Send Heartbeat regardless of readers QosReliability
		}
	}

	/**
	 * Close this writer. Closing a writer clears its cache of changes.
	 */
	public void close() {
		announceThread.cancel(true);
		readerProxies.clear();
		//writer_cache.clear();
	}

	/**
	 * Add a matched reader.
	 * 
	 * @param readerData
	 * @return ReaderProxy
	 */
	public ReaderProxy addMatchedReader(SubscriptionData readerData) {
		ReaderProxy proxy = new ReaderProxy(readerData);
		addLocators(proxy);
		readerProxies.put(readerData.getKey(), proxy);

		QosDurability readerDurability = 
				(QosDurability) readerData.getQualityOfService().getPolicy(QosDurability.class);

		if (QosDurability.Kind.VOLATILE == readerDurability.getKind()) {
			// VOLATILE readers are marked having received all the samples so far
			log.trace("[{}] Setting highest seqNum to {} for VOLATILE reader", getGuid().getEntityId(), 
					writer_cache.getSeqNumMax());

			proxy.setReadersHighestSeqNum(writer_cache.getSeqNumMax());
		}
		else { 
			// Otherwise, send either HB, or our history
			Guid guid = readerData.getKey();

			if (proxy.isReliable()) {
				sendHeartbeat(guid.getPrefix(), guid.getEntityId());
			}
			else {
				sendData(guid.getPrefix(), guid.getEntityId(), proxy.getReadersHighestSeqNum());
				proxy.setReadersHighestSeqNum(writer_cache.getSeqNumMax());
			}
		}

		log.debug("[{}] Added matchedReader {}", getGuid().getEntityId(), readerData);
		return proxy;
	}

	/**
	 * Removes all the matched writers that have a given GuidPrefix
	 * @param prefix
	 */
	public void removeMatchedReaders(GuidPrefix prefix) {
		for (ReaderProxy rp : readerProxies.values()) {
			if (prefix.equals(rp.getGuid().getPrefix())) {
				removeMatchedReader(rp.getSubscriptionData());
			}
		}
	}

	/**
	 * Remove a matched reader.
	 * @param readerData
	 */
	public void removeMatchedReader(SubscriptionData readerData) {
		readerProxies.remove(readerData.getKey());
		log.debug("[{}] Removed matchedReader {}, {}", getGuid().getEntityId(), readerData.getKey());
	}

	/**
	 * Gets all the matched readers of this RTPSWriter
	 * @return a Collection of matched readers
	 */
	public Collection<ReaderProxy> getMatchedReaders() {
		return readerProxies.values();
	}

	/**
	 * Gets the matched readers owned by given remote participant.
	 * 
	 * @param prefix GuidPrefix of the remote participant
	 * @return a Collection of matched readers
	 */
	public Collection<ReaderProxy> getMatchedReaders(GuidPrefix prefix) {
		List<ReaderProxy> proxies = new LinkedList<>();
		for (Guid guid : readerProxies.keySet()) {
			if (guid.getPrefix().equals(prefix)) {
				proxies.add(readerProxies.get(guid));
			}
		}
		return proxies;	
	}

	/**
	 * Handle incoming AckNack message.
	 * 
	 * @param senderPrefix
	 * @param ackNack
	 */
	void onAckNack(GuidPrefix senderPrefix, AckNack ackNack) {
		log.debug("[{}] Got AckNack: #{} {}, F:{} from {}", getGuid().getEntityId(), 
				ackNack.getCount(), ackNack.getReaderSNState(), ackNack.finalFlag(), senderPrefix);

		ReaderProxy proxy = readerProxies.get(new Guid(senderPrefix, ackNack.getReaderId()));
		if (proxy != null) {
			if(proxy.ackNackReceived(ackNack)) {
				log.trace("[{}] Wait for nack response delay: {} ms", getGuid().getEntityId(), nackResponseDelay);
				getParticipant().waitFor(nackResponseDelay);

				sendData(senderPrefix, ackNack.getReaderId(), ackNack.getReaderSNState().getBitmapBase() - 1);
			}
			else {
				log.debug("[{}] Ignoring AckNack whose count is {}, since proxys count is {}", getGuid().getEntityId(), ackNack.getCount(), proxy.getLatestAckNackCount());
			}
		} 
		else {
			log.warn("[{}] Discarding AckNack from unknown reader {}", getGuid().getEntityId(), ackNack.getReaderId());
		}
	}


	/**
	 * Send data to given participant & reader. readersHighestSeqNum specifies
	 * which is the first data to be sent.
	 * 
	 * @param targetPrefix
	 * @param readerId
	 * @param readersHighestSeqNum
	 */
	private void sendData(GuidPrefix targetPrefix, EntityId readerId, long readersHighestSeqNum) {
		Message m = new Message(getGuid().getPrefix());
		SortedSet<CacheChange> changes = writer_cache.getChangesSince(readersHighestSeqNum);

		if (changes.size() == 0) {
			log.trace("[{}] sendData() called, but no changes since {}. returning.", getGuid().getEntityId(), readersHighestSeqNum);
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
				log.warn("[{}] Failed to add cache change to message", getGuid().getEntityId(), ioe);
			}
		}

		log.debug("[{}] Sending Data: {}-{} to {}", getGuid().getEntityId(), firstSeqNum, lastSeqNum, targetPrefix);

		boolean overFlowed = sendMessage(m, targetPrefix); 
		if (overFlowed) {
			log.trace("Sending of Data overflowed. Sending HeartBeat to notify reader.");
			sendHeartbeat(targetPrefix, readerId);
		}
	}

	private void sendHeartbeat(GuidPrefix senderPrefix, EntityId readerId) {
		sendHeartbeat(senderPrefix, readerId, false);
	}

	private void sendHeartbeat(GuidPrefix targetPrefix, EntityId readerId, boolean livelinessFlag) {
		Message m = new Message(getGuid().getPrefix());
		Heartbeat hb = createHeartbeat(readerId);
		hb.livelinessFlag(livelinessFlag);
		m.addSubMessage(hb);

		log.debug("[{}] Sending Heartbeat: #{} {}-{}, F:{}, L:{} to {}", getGuid().getEntityId(), 
				hb.getCount(), hb.getFirstSequenceNumber(), hb.getLastSequenceNumber(), 
				hb.finalFlag(), hb.livelinessFlag(), targetPrefix);

		//		Exception e = new Exception();
		//		e.printStackTrace();

		sendMessage(m, targetPrefix);

		if (!livelinessFlag) {
			ReaderProxy proxy = readerProxies.get(new Guid(targetPrefix, readerId));
			if (proxy != null) {
				proxy.heartbeatSent();
			}
		}		
	}

	private Heartbeat createHeartbeat(EntityId entityId) {
		if (entityId == null) {
			entityId = EntityId.UNKNOWN_ENTITY;
		}

		Heartbeat hb = new Heartbeat(entityId, getGuid().getEntityId(),
				writer_cache.getSeqNumMin(), writer_cache.getSeqNumMax(), hbCount++ );

		return hb;
	}


	private Data createData(EntityId readerId, CacheChange cc) throws IOException {		
		DataEncapsulation dEnc = cc.getDataEncapsulation();
		ParameterList inlineQos = new ParameterList();

		if (cc.hasKey()) { // Add KeyHash if present
			inlineQos.add(cc.getKey());
		}

		if (!cc.getKind().equals(CacheChange.Kind.WRITE)) { // Add status info for operations other than WRITE
			inlineQos.add(new StatusInfo(cc.getKind()));
		}

		Data data = new Data(readerId, getGuid().getEntityId(), cc.getSequenceNumber(), inlineQos, dEnc);

		return data;
	}


	/**
	 * Checks, if a given change number has been acknowledged by every known
	 * matched reader.
	 * 
	 * @param sequenceNumber sequenceNumber of a change to check
	 * @return true, if every matched reader has acknowledged given change number
	 */
	public boolean isAcknowledgedByAll(long sequenceNumber) {
		for (ReaderProxy proxy : readerProxies.values()) {
			if (proxy.isActive() && proxy.getReadersHighestSeqNum() < sequenceNumber) {
				return false;
			}
		}

		return true;
	}


	public boolean isMatchedWith(SubscriptionData readerData) {
		return readerProxies.get(readerData.getKey()) != null;
	}
}
