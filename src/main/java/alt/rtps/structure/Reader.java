package alt.rtps.structure;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.discovery.WriterData;
import alt.rtps.message.Data;
import alt.rtps.message.Heartbeat;
import alt.rtps.types.Duration_t;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.GuidPrefix_t;
import alt.rtps.types.Time_t;

public abstract class Reader extends Endpoint {
	static final Logger log = LoggerFactory.getLogger(Reader.class);

	private List<WriterData> matchedWriters = new LinkedList<WriterData>();
	
	/**
	 * Contains the history caches of matching writers. Each Reader may be matched with multiple writers.
	 */
	private HashMap<GuidPrefix_t, HistoryCache> readerCaches = new HashMap<>();
	
	
	/**
	 * Specifies whether the RTPS Reader expects in-line QoS to be sent along with any data.
	 */
	boolean expectsInlineQos = false;
	/**
	 * Protocol tuning parameter that allows the RTPS Reader to ignore HEARTBEATs that
	 * arrive ‘too soon’ after a previous HEARTBEAT was received.
	 */
	Duration_t heartbeatSuppressionDuration = new Duration_t(0, 0);
	
	Duration_t heartbeatResponseDelay = new Duration_t(0, 500000000); // 500 ms

	/**
	 * 
	 * @param prefix prefix from the participant that creates this Reader
	 * @param entityId
	 * @param topicName 
	 */
	public Reader(GuidPrefix_t prefix, EntityId_t entityId, String topicName) {
		super(prefix, entityId, topicName);
	}

	protected HistoryCache getHistoryCache(GuidPrefix_t writerPrefix) {
		HistoryCache historyCache = readerCaches.get(writerPrefix);
		if (historyCache == null) {
			log.debug("Creating new HistoryCache for writer {}", writerPrefix);
			historyCache = new HistoryCache();
			readerCaches.put(writerPrefix, historyCache);
		}
		
		return historyCache;
	}
	
	
	public void addMatchedWriter(WriterData writerData) {
		
		log.debug("Adding matched writer for {}", writerData.getWriterGuid());
		matchedWriters.add(writerData);
	}

	public abstract void onData(GuidPrefix_t prefix, Data data, Time_t timestamp);

	public abstract void onHeartbeat(GuidPrefix_t senderGuidPrefix, Heartbeat hb);

	public int endpointId() {
		// TODO Auto-generated method stub
		return 0;
	}
}

