package alt.rtps;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.builtin.ReaderData;
import alt.rtps.message.AckNack;
import alt.rtps.message.Data;
import alt.rtps.message.Heartbeat;
import alt.rtps.message.InfoTimestamp;
import alt.rtps.message.Message;
import alt.rtps.message.data.DataEncapsulation;
import alt.rtps.transport.UDPWriter;
import alt.rtps.types.Duration_t;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.GUID_t;
import alt.rtps.types.GuidPrefix_t;
import alt.rtps.types.Locator_t;
import alt.rtps.types.Time_t;

/**
 * RTPSWriter implements RTPS writer endpoint.
 * 
 * @author mcr70
 *
 */
public class RTPSWriter extends Endpoint {
	public enum ChangeKind {
		WRITE, DISPOSE, UNREGISTER;
	}


	private static final Logger log = LoggerFactory.getLogger(RTPSWriter.class);

	private HashSet<ReaderData> matchedReaders = new HashSet<>();
	private Thread resendThread;
	private boolean running;
	private CyclicBarrier barrier;

	/**
	 * Protocol tuning parameter that indicates that the StatelessWriter resends
	 * all the changes in the writer’s HistoryCache to all the Locators
	 * periodically each resendPeriod.
	 */
	private Duration_t resendDataPeriod = null;//new Duration_t(30, 0);

	private final Marshaller marshaller;
	private final HistoryCache writer_cache;
	private int hbCount; // heartbeat counter. incremented each time hb is sent
	protected Object resend_lock = new Object();


	public RTPSWriter(GuidPrefix_t prefix, EntityId_t entityId, String topicName, Marshaller marshaller) {
		super(prefix, entityId, topicName);

		this.writer_cache = new HistoryCache(new GUID_t(prefix, entityId));
		this.marshaller = marshaller;
	}



	public void setResendDataPeriod(Duration_t period, final EntityId_t readerId) {
		resendDataPeriod = period;
		barrier = new CyclicBarrier(2);

		resendThread = new Thread() {
			@Override
			public void run() {
				running = true;

				while (running) {
					List<CacheChange> changes = writer_cache.getChanges();

					for (CacheChange change : changes) { // TODO: ConcurrentModification
						try {
							Message m = new Message(getGuid().prefix);

							InfoTimestamp iTime = new InfoTimestamp(new Time_t((int)System.currentTimeMillis(), (int)System.nanoTime()));
							m.addSubMessage(iTime);

							DataEncapsulation dEnc = marshaller.marshall(change.getData());
							Data data = new Data(readerId, getGuid().entityId, change.getSequenceNumber(), null, dEnc);
							m.addSubMessage(data);

							log.debug("[{}] Send {}, {}: {}", getGuid().entityId, 
									change.getData().getClass().getSimpleName(), 
									change.getSequenceNumber(), change.getData());

							sendMessage(m, null);
						}
						catch(IOException ioe) {
							log.warn("Failed to send cache change {}", change);
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

	void sendHistoryCache(Locator_t locator, EntityId_t readerId) { // TODO: Can we get rid of this.
		//if (true) return;

		Message m = new Message(getGuid().prefix);
		List<CacheChange> changes = writer_cache.getChanges();

		for (CacheChange cc : changes) {
			log.trace("[{}] Marshalling {}", getGuid().entityId, cc.getData());
			try {
				DataEncapsulation dEnc = marshaller.marshall(cc.getData()); 
				Data data = new Data(readerId, getGuid().entityId, cc.getSequenceNumber(), null, dEnc);

				m.addSubMessage(data);
			}
			catch(IOException ioe) {
				log.warn("Failed to add cache change to message", ioe);
			}
		}

		log.debug("[{}] Sending history cache to {}: {}", getGuid().entityId, locator, m);

		try {
			UDPWriter u = new UDPWriter(locator);
			u.sendMessage(m, "c:/tmp/" + getGuid().entityId + "-hc.rtps");
			u.close();
		}
		catch(IOException ioe) {
			log.warn("Failed to send HistoryCache: {}", ioe);
		}
	}

	/**
	 * Creates a new cache change to history cache. Note, that matced readers are not notified automatically
	 * of changes in history cache. Use sendHeartbeat() method to notify remote readers of changes in history cache.
	 * This way, multiple changes can be notified only once.
	 * <p> 
	 * As a side effect, Message sent as a response to readers AckNack message can (and will) contain 
	 * multiple Data submessages in one UDP packet.
	 * 
	 * @param obj
	 * @see sendHeartbeat()
	 */
	public void createChange(ChangeKind kind, Object obj) {
		writer_cache.createChange(obj);	
	}

	public void createChange(Object obj) {
		createChange(ChangeKind.WRITE, obj);	
	}

	public void close() {
		// TODO: This is not working at the moment
		if (barrier != null) {
			try {
				barrier.await(15, TimeUnit.SECONDS);
			} catch (Exception e) {
				log.warn("Exception ", e);
			}

			if (running) {
				resendThread.interrupt();
			}
		}

		writer_cache.getChanges().clear();
	}


	void addMatchedReader(ReaderData readerData) {
		matchedReaders.add(readerData);
		log.debug("Adding matchedReader {}", readerData);
		sendHeartbeat(readerData.getKey());
	}

	/**
	 * Handle incoming AckNack message.
	 * 
	 * @param senderPrefix
	 * @param ackNack
	 */
	public void onAckNack(GuidPrefix_t senderPrefix, AckNack ackNack) {
		log.debug("[{}] Got AckNack: {}", getGuid().entityId, ackNack.getReaderSNState());

		if (writer_cache.size() > 0) {
			sendData(senderPrefix, ackNack);
		}
		else { // Send HB / GAP to reader so that it knows our state
			if (ackNack.finalFlag()) { // FinalFlag indicates whether a response by the Writer is expected
				sendHeartbeat(senderPrefix, ackNack);
			}
		}
	}

	private void sendData(GuidPrefix_t senderPrefix, AckNack ackNack) {
		Message m = new Message(getGuid().prefix);
		List<CacheChange> changes = writer_cache.getChanges();
		long lastSeqNum = 0;
		for (CacheChange cc : changes) {
			log.trace("Marshalling {}", cc.getData());
			try {
				lastSeqNum = cc.getSequenceNumber();
				if (lastSeqNum >= ackNack.getReaderSNState().getBitmapBase()) {
					DataEncapsulation dEnc = marshaller.marshall(cc.getData()); 
					Data data = new Data(ackNack.getReaderId(), getGuid().entityId, cc.getSequenceNumber(), null, dEnc);

					m.addSubMessage(data);
				}
			}
			catch(IOException ioe) {
				log.warn("Failed to add cache change to message", ioe);
			}
		}

		log.debug("[{}] Sending Data: {}", getGuid().entityId, lastSeqNum);
		sendMessage(m, senderPrefix); 
	}

	private void sendHeartbeat(GuidPrefix_t senderPrefix, AckNack ackNack) {
		Message m = new Message(getGuid().prefix);
		Heartbeat hb = createHeartbeat(ackNack.getReaderId());
		m.addSubMessage(hb);

		log.debug("[{}] Sending Heartbeat: {}-{}", getGuid().entityId, hb.getFirstSequenceNumber(), hb.getLastSequenceNumber());
		sendMessage(m, senderPrefix);
	}

	private Heartbeat createHeartbeat(EntityId_t entityId) {
		if (entityId == null) {
			entityId = EntityId_t.UNKNOWN_ENTITY;
		}

		Heartbeat hb = new Heartbeat(entityId, getGuid().entityId,
				writer_cache.getSeqNumMin(), writer_cache.getSeqNumMax(), hbCount++ );

		return hb;
	}

	/**
	 * Sends a Heartbeat message to every matched RTPSReader. By sending a Heartbeat message, remote readers 
	 * know about Data samples available on this writer.
	 * 
	 */
	public void sendHeartbeat() {		
		log.debug("[{}] Sending Heartbeat to {} matched readers", getGuid().entityId, matchedReaders.size());
		for (ReaderData rd : matchedReaders) {
			sendHeartbeat(rd.getKey());
		}
	}

	private void sendHeartbeat(GUID_t readerGuid) {
		log.debug("[{}] Sending Heartbeat to {}", getGuid().entityId, readerGuid);
		
		Message m = new Message(getGuid().prefix);
		Heartbeat hb = createHeartbeat(readerGuid.entityId); 
		m.addSubMessage(hb);

		sendMessage(m, readerGuid.prefix);
	}
}
