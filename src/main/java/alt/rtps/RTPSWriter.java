package alt.rtps;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.message.AckNack;
import alt.rtps.message.Data;
import alt.rtps.message.Heartbeat;
import alt.rtps.message.Message;
import alt.rtps.transport.Marshaller;
import alt.rtps.types.Duration_t;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.GuidPrefix_t;

/**
 * 
 * @author mcr70
 *
 */
public class RTPSWriter extends Writer {
	private static final Logger log = LoggerFactory.getLogger(RTPSWriter.class);
	private Thread statelessResenderThread;
	private boolean running;
	
	/**
	 * Protocol tuning parameter that indicates that the StatelessWriter resends
	 * all the changes in the writer’s HistoryCache to all the Locators
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
						Message m = marshaller.toMessage(getGuid().prefix, change.getData());
						
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
			Data d = createData(cc);
			m.addSubMessage(d);
		}
	}

	private Data createData(CacheChange cc) {
		Data d = marshaller.marshall(cc.getData());
		
		return d;
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
