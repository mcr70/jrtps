package alt.rtps;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.builtin.ReaderData;
import alt.rtps.message.AckNack;
import alt.rtps.message.Data;
import alt.rtps.message.Heartbeat;
import alt.rtps.message.InfoTimestamp;
import alt.rtps.message.Message;
import alt.rtps.message.data.DataEncapsulation;
import alt.rtps.transport.Marshaller;
import alt.rtps.transport.UDPWriter;
import alt.rtps.types.Duration_t;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.GUID_t;
import alt.rtps.types.GuidPrefix_t;
import alt.rtps.types.Locator_t;
import alt.rtps.types.Time_t;

/**
 * 
 * @author mcr70
 *
 */
public class RTPSWriter extends Endpoint {
	private static final Logger log = LoggerFactory.getLogger(RTPSWriter.class);
	
	private HashSet<ReaderData> matchedReaders = new HashSet<>();
	private Thread statelessResenderThread;
	private boolean running;

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


	/**
	 * Get the HistoryCache of this RTPSWriter
	 * 
	 * @return
	 */
	HistoryCache getHistoryCache() {
		return writer_cache;
	}

	public void setResendDataPeriod(Duration_t period, final EntityId_t readerId) {
		resendDataPeriod = period;

		statelessResenderThread = new Thread() {
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
					
					synchronized (resend_lock ) {
						try {
							Thread.sleep(resendDataPeriod.sec * 1000);
						} catch (InterruptedException e) { 
							running = false;
						}
					}
				}

				log.debug("[{}] Resend thread dying", getGuid().entityId);
			}
		};

		log.debug("[{}] Starting resend thread with period {}", getGuid().entityId, period);
		statelessResenderThread.start();
	}

	public void onAckNack(GuidPrefix_t senderPrefix, AckNack ackNack) {
		log.debug("[{}] Got {}", getGuid().entityId, ackNack);

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

		for (CacheChange cc : changes) {
			log.trace("Marshalling {}", cc.getData());
			try {
				DataEncapsulation dEnc = marshaller.marshall(cc.getData()); 
				Data data = new Data(ackNack.getReaderId(), getGuid().entityId, cc.getSequenceNumber(), null, dEnc);

				m.addSubMessage(data);
			}
			catch(IOException ioe) {
				log.warn("Failed to add cache change to message", ioe);
			}
		}

		log.debug("[{}] Sending {}", getGuid().entityId, m);
		sendMessage(m, senderPrefix); 
	}

	private void sendHeartbeat(GuidPrefix_t senderPrefix, AckNack ackNack) {
		Message m = new Message(getGuid().prefix);
		Heartbeat hb = createHeartbeat();
		m.addSubMessage(hb);

		log.debug("[{}] Sending {}", getGuid().entityId, m);
		sendMessage(m, senderPrefix);
	}

	private Heartbeat createHeartbeat() {

		Heartbeat hb = new Heartbeat(EntityId_t.UNKNOWN_ENTITY, getGuid().entityId,
				writer_cache.getSeqNumMin(), writer_cache.getSeqNumMax(), hbCount++ );

		return hb;
	}

	/**
	 * Get the BuiltinEndpointSet ID of this RTPSWriter.
	 * 
	 * @return 0, if this RTPSWriter is not builtin endpoint
	 */
	int endpointSetId() {
		return getGuid().entityId.getEndpointSetId();
	}

	public void sendHistoryCache(Locator_t locator, EntityId_t readerId) {
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


	public void createChange(Object obj) {
		getHistoryCache().createChange(obj);	
	}


	public void close() {
		//resend_lock.notify();
		writer_cache.getChanges().clear();
	}


	void addMatchedReader(ReaderData readerData) {
		matchedReaders.add(readerData);
	}
}
