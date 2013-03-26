package alt.rtps;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.message.AckNack;
import alt.rtps.message.Data;
import alt.rtps.message.DataEncapsulation;
import alt.rtps.message.Heartbeat;
import alt.rtps.message.InfoTimestamp;
import alt.rtps.message.Message;
import alt.rtps.transport.Marshaller;
import alt.rtps.types.Duration_t;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.GuidPrefix_t;
import alt.rtps.types.Time_t;

/**
 * 
 * @author mcr70
 *
 */
public class RTPSWriter extends Writer {
	private static final Logger log = LoggerFactory.getLogger(RTPSWriter.class);
	private Thread statelessResenderThread;
	private boolean running;
	private long seqNum = 0;
	
	/**
	 * Protocol tuning parameter that indicates that the StatelessWriter resends
	 * all the changes in the writer�s HistoryCache to all the Locators
	 * periodically each resendPeriod.
	 */
	private Duration_t resendDataPeriod = null;//new Duration_t(30, 0);

	private final Marshaller marshaller;
	private int hbCount; // heartbeat counter. incremented each time hb is sent

	public RTPSWriter(GuidPrefix_t prefix, EntityId_t entityId, String topicName, Marshaller marshaller) {
		super(prefix, entityId, topicName);
		
		this.marshaller = marshaller;
	}

	public void setResendDataPeriod(Duration_t period) {
		resendDataPeriod = period;
		
		statelessResenderThread = new Thread() {
			@Override
			public void run() {
				running = true;
				
				while (running) {
					List<CacheChange> changes = writer_cache.getChanges();
					log.debug("Sending " + changes.size() + " changes");
					for (CacheChange change : changes) { // TODO: ConcurrentModification
						Message m1 = new Message(getGuid().prefix);
						
						InfoTimestamp iTime = new InfoTimestamp(new Time_t((int)System.currentTimeMillis(), (int)System.nanoTime()));
						m1.addSubMessage(iTime);
						
						DataEncapsulation dEnc = marshaller.marshall(change.getData());
						Data data = new Data(EntityId_t.UNKNOWN_ENTITY, getGuid().entityId, seqNum++, null, dEnc);
						
						m1.addSubMessage(data);
						
						Message m = m1;
						
						sendToLocators(m, getMatchedEndpointLocators());
					}
					
					synchronized (writer_cache) {
						try {
							writer_cache.wait(resendDataPeriod.sec * 1000);
						} catch (InterruptedException e) { }
					}
				}
				
				log.debug("Resend thread dying");
			}
		};
	
		log.debug("Starting resend thread for {} with period {}", getGuid().entityId, period);
		statelessResenderThread.start();
	}

	public void onAckNack(GuidPrefix_t senderPrefix, AckNack ackNack) {
		log.debug("{}", ackNack);
		
		HistoryCache hc = getHistoryCache();
		if (hc.size() > 0) {
			sendData(senderPrefix, ackNack);
		}
		else { // Send HB / GAP to reader so that it knows our state
			sendHeartBeat(senderPrefix, ackNack); 
		}
	}

	private void sendData(GuidPrefix_t senderPrefix, AckNack ackNack) {
		Message m = new Message(getGuid().prefix);
		List<CacheChange> changes = getHistoryCache().getChanges();
		
		for (CacheChange cc : changes) {
			DataEncapsulation dEnc = marshaller.marshall(cc.getData()); 
			Data data = new Data(ackNack.getReaderId(), getGuid().entityId, seqNum++, null, dEnc);
			
			m.addSubMessage(data);
		}
	}

	private void sendHeartBeat(GuidPrefix_t senderPrefix, AckNack ackNack) {
		Message m = new Message(getGuid().prefix);
		Heartbeat hb = createHeartbeat();
		m.addSubMessage(hb);

		sendMessage(m, senderPrefix);
	}

	private Heartbeat createHeartbeat() {
		HistoryCache hc = getHistoryCache();
		Heartbeat hb = new Heartbeat(EntityId_t.UNKNOWN_ENTITY, getGuid().entityId,
				hc.getSeqNumMin(), hc.getSeqNumMax(), hbCount++ );
		
		return hb;
	}
}
