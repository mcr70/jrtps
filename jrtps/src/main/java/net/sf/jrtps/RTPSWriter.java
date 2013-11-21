package net.sf.jrtps;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.sf.jrtps.builtin.ReaderData;
import net.sf.jrtps.message.AckNack;
import net.sf.jrtps.message.Data;
import net.sf.jrtps.message.Heartbeat;
import net.sf.jrtps.message.InfoTimestamp;
import net.sf.jrtps.message.Message;
import net.sf.jrtps.message.data.DataEncapsulation;
import net.sf.jrtps.message.parameter.KeyHash;
import net.sf.jrtps.message.parameter.ParameterList;
import net.sf.jrtps.message.parameter.StatusInfo;
import net.sf.jrtps.types.Duration_t;
import net.sf.jrtps.types.EntityId_t;
import net.sf.jrtps.types.GUID_t;
import net.sf.jrtps.types.GuidPrefix_t;
import net.sf.jrtps.types.Time_t;

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
	
	private HashSet<ReaderData> matchedReaders = new HashSet<>();
	private Thread resendThread;
	private boolean running;
	private CyclicBarrier barrier;

	/**
	 * Protocol tuning parameter that indicates that the StatelessWriter resends
	 * all the changes in the writer's HistoryCache to all the Locators
	 * periodically each resendPeriod.
	 */
	private Duration_t resendDataPeriod = null;//new Duration_t(30, 0);

	@SuppressWarnings("rawtypes")
	private final Marshaller marshaller;
	private final HistoryCache writer_cache;
	private int hbCount; // heartbeat counter. incremented each time hb is sent
	protected Object resend_lock = new Object();


	public RTPSWriter(GuidPrefix_t prefix, EntityId_t entityId, String topicName, Marshaller<?> marshaller, 
			QualityOfService qos, Configuration configuration) {
		super(prefix, entityId, topicName, qos, configuration);

		this.writer_cache = new HistoryCache(new GUID_t(prefix, entityId));
		this.marshaller = marshaller;
		
		try {
			this.md5 = MessageDigest.getInstance("MD5");
		} 
		catch (NoSuchAlgorithmException e) {
			// Just warn. Actual usage might not even need it.
			log.warn("There is no MD5 algorithm available", e);
		}
	}


	/**
	 * Sets the period at which data gets resend to recipients
	 * @param period
	 * @param readerId
	 */
	void setResendDataPeriod(Duration_t period, final EntityId_t readerId) {
		// TODO: This is used by SPDP to announce republish ParticipantData periodically.
		//       Should this be removed from RTPSWriter? I think so.
		//       SPDPClient?, SEDPClient?
		resendDataPeriod = period;
		barrier = new CyclicBarrier(2);

		resendThread = new Thread() {
			@Override
			public void run() {
				running = true;

				while (running) {
					List<CacheChange> changes = writer_cache.getChanges();

					for (CacheChange cc : changes) { // TODO: ConcurrentModification
						try {
							Message m = new Message(getGuid().prefix);

							InfoTimestamp iTime = new InfoTimestamp(new Time_t(System.currentTimeMillis()));
							m.addSubMessage(iTime);

							Data data = createData(readerId, cc);
							m.addSubMessage(data);

							log.debug("[{}] Send {}, {}: {}", getGuid().entityId, 
									cc.getData().getClass().getSimpleName(), 
									cc.getSequenceNumber(), cc.getData());

							sendMessage(m, null);
						}
						catch(IOException ioe) {
							log.warn("Failed to send cache change {}", cc);
						}
					}

					try {
						barrier.await(resendDataPeriod.sec, TimeUnit.SECONDS);
					} catch (TimeoutException te) {
						barrier.reset();
					} catch (Exception e) {
						running = false;
					}
				}

				log.debug("[{}] Resend thread dying", getGuid().entityId);
			}
		};

		log.debug("[{}] Starting resend thread with period {}", getGuid().entityId, period);
		resendThread.start();
	}

	/**
	 * Get the BuiltinEndpointSet ID of this RTPSWriter.
	 * 
	 * @return 0, if this RTPSWriter is not builtin endpoint
	 */
	int endpointSetId() {
		return getGuid().entityId.getEndpointSetId();
	}

	/**
	 * Creates a new cache change to history cache. Note, that matched readers are not notified automatically
	 * of changes in history cache. Use sendHeartbeat() method to notify remote readers of changes in history cache.
	 * This way, multiple changes can be notified only once.
	 * <p> 
	 * As a side effect, Message sent as a response to readers AckNack message can (and will) contain 
	 * multiple Data submessages in one UDP packet.
	 * 
	 * @param kind
	 * @param obj
	 * @see #sendHeartbeat()
	 */
	public void createChange(ChangeKind kind, T obj) {
		writer_cache.createChange(kind, obj);	
	}

	public void createChange(T obj) {
		createChange(ChangeKind.WRITE, obj);	
	}

	/**
	 * Sends a Heartbeat message to every matched RTPSReader. By sending a Heartbeat message, 
	 * remote readers know about Data samples available on this writer.<p>
	 * 
	 * Heartbeat is not sent automatically. This provides means to create multiple changes,
	 * before announcing the state to readers.
	 * 
	 */
	public void sendHeartbeat() {		
		log.debug("[{}] Sending Heartbeat to {} matched readers", getGuid().entityId, matchedReaders.size());
		for (ReaderData rd : matchedReaders) {
			GUID_t guid = rd.getKey();
			sendHeartbeat(guid.prefix, guid.entityId);
		}
	}

	/**
	 * Assert liveliness of this writer. Matched readers are notified via
	 * Heartbeat message of the liveliness of this writer.
	 */
	public void assertLiveliness() {
		for (ReaderData rd : matchedReaders) {
			GUID_t guid = rd.getKey();
			sendHeartbeat(guid.prefix, guid.entityId, true);
		}
	}
	
	/**
	 * Close this writer.
	 */
	public void close() {
		if (barrier != null) {
			try {
				barrier.await(15, TimeUnit.SECONDS);
			} catch (Exception e) {
				log.warn("Got Exception on close()", e);
			}

			if (running) {
				resendThread.interrupt();
			}
		}

		writer_cache.getChanges().clear();
	}


	void removeMatchedReader(ReaderData readerData) {
		log.debug("Removing matchedReader {}", readerData);
		matchedReaders.remove(readerData);
	}
	
	void addMatchedReader(ReaderData readerData) {
		matchedReaders.add(readerData);
		log.debug("Adding matchedReader {}", readerData);
		GUID_t guid = readerData.getKey();
		sendHeartbeat(guid.prefix, guid.entityId);
	}

	/**
	 * Handle incoming AckNack message.
	 * 
	 * @param senderPrefix
	 * @param ackNack
	 */
	void onAckNack(GuidPrefix_t senderPrefix, AckNack ackNack) {
		log.debug("[{}] Got AckNack: {}", getGuid().entityId, ackNack.getReaderSNState());

		if (writer_cache.size() > 0) {
			sendData(senderPrefix, ackNack.getReaderId(), ackNack.getReaderSNState().getBitmapBase());
		}
		else { // Send HB / GAP to reader so that it knows our state
			if (ackNack.finalFlag()) { // FinalFlag indicates whether a response by the Writer is expected
				sendHeartbeat(senderPrefix, ackNack.getReaderId());
			}
		}
	}


	/**
	 * Send data to given participant & reader. readersHighestSeqNum specifies
	 * Which is the first data to be sent.
	 * 
	 * @param senderPrefix
	 * @param readerId
	 * @param readersHighestSeqNum
	 */
	void sendData(GuidPrefix_t senderPrefix, EntityId_t readerId, long readersHighestSeqNum) {
		Message m = new Message(getGuid().prefix);
		List<CacheChange> changes = writer_cache.getChanges();
		long lastSeqNum = 0;
		long firstSeqNum = 0;
		long prevTimeStamp = 0;
		
		for (CacheChange cc : changes) {			
			try {
				lastSeqNum = cc.getSequenceNumber();
				//if (lastSeqNum >= ackNack.getReaderSNState().getBitmapBase()) {
				if (lastSeqNum >= readersHighestSeqNum) {
					long timeStamp = cc.getTimeStamp();
					if (timeStamp > prevTimeStamp) {
						InfoTimestamp infoTS = new InfoTimestamp(timeStamp);
						log.debug("Adding {}", infoTS);
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
				log.warn("Failed to add cache change to message", ioe);
			}
		}

		log.debug("[{}] Sending Data: {}-{}", getGuid().entityId, firstSeqNum, lastSeqNum);
		boolean overFlowed = sendMessage(m, senderPrefix); 
		if (overFlowed) {
			sendHeartbeat(senderPrefix, readerId);
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
}
