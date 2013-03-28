package alt.rtps;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import alt.rtps.builtin.ReaderData;
import alt.rtps.types.Duration_t;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.GUID_t;
import alt.rtps.types.GuidPrefix_t;
/**
 * 
 * @author mcr70
 * @see 8.4.7.1
 */
public abstract class Writer extends Endpoint {
	private static final Logger log = LoggerFactory.getLogger(Writer.class);

	private List<ReaderData> matchedReaders = new LinkedList<ReaderData>();
	
	/**
	 * Configures the mode in which the Writer operates. If pushMode==true, then the
	 * Writer will push changes to the reader. If pushMode==false, changes will only be 
	 * announced via heartbeats and only be sent as response to the request of a reader.
	 */
	protected boolean pushMode = true;
	
	private Duration_t heartbeatPeriod = new Duration_t(5, 0); // 5 sec, tunable
	private Duration_t nackResponseDelay = new Duration_t(0, 200000000); // 200 ms
	private Duration_t nackSuppressionDuration = new Duration_t(0, 0); // 0, tunable
	
	/**
	 * Contains the history of CacheChange changes for this Writer.
	 */
	protected HistoryCache writer_cache;
	

	/**
	 * 
	 * @param prefix prefix from the participant that creates this Writer
	 * @param entityId
	 * @param topicName 
	 */
	public Writer(GuidPrefix_t prefix, EntityId_t entityId, String topicName) {
		super(prefix, entityId, topicName);
		
		writer_cache = new HistoryCache(new GUID_t(prefix, entityId));
	}
		
	
	/**
	 * Protocol tuning parameter that allows the RTPS Writer to repeatedly announce the
	 * availability of data by sending a Heartbeat Message.
	 * @return
	 */
	public Duration_t heartbeatPeriod() {
		return heartbeatPeriod;
	}
	
	/**
	 * Protocol tuning parameter that allows the RTPS Writer to delay
	 * the response to a request for data from a negative acknowledgment.
	 * <p>
	 * See chapter 8.4.7.1.1 for default values 
	 * @return
	 */
	public Duration_t nackResponseDelay() {
		return nackResponseDelay;
	}
	
	/**
	 * Protocol tuning parameter that allows the RTPS Writer to ignore requests for data from
	 * negative acknowledgments that arrive �too soon� after the corresponding change is sent.
	 * <p>
	 * See chapter 8.4.7.1.1 for default values 
	 * @return 
	 * 
	 */
	public Duration_t nackSupressionDuration() {
		return nackSuppressionDuration;
	}
	


	protected HistoryCache getHistoryCache() {
		return writer_cache;
	}	
}
