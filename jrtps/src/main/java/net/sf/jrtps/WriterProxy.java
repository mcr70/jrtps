package net.sf.jrtps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jrtps.builtin.PublicationData;
import net.sf.jrtps.types.Guid;


/**
 * WriterProxy represents a remote writer. 
 * 
 * @author mcr70
 *
 */
public class WriterProxy {
	private static final Logger log = LoggerFactory.getLogger(WriterProxy.class);
	private final Guid writerGuid;
	private /*final*/ PublicationData writerData; // TODO: should be final
	
	private volatile long livelinessTimestamp;
	private volatile long seqNumMax = 0;
	
	WriterProxy(PublicationData wd) {
		this.writerData = wd;
		writerGuid = wd.getKey();
	}
	
	WriterProxy(Guid writerGuid) {
		this.writerGuid = writerGuid; // TODO: this constructor should be removed
	}
	
	/**
	 * Gets the guid of the writer represented by this WriterProxy.
	 * @return Guid
	 */
	public Guid getGuid() {
		return writerGuid;
	}

	long getSeqNumMax() {
		return seqNumMax;
	}
	
	/**
	 * Gets the WriterData associated with this WriterProxy.
	 * @return WriterData
	 */
	public PublicationData getPublicationData() {
		return writerData;
	}
	
	/**
	 * Determines if incoming Data should be accepted or not.
	 * 
	 * @param sequenceNumber
	 * @return true, if data was added to cache
	 */
	boolean acceptData(long sequenceNumber) {
		// Data must come in order. If not, drop it. Manage out-of-order data with 
		// HeartBeat & AckNack messages

		if (sequenceNumber > seqNumMax) { 
			if (sequenceNumber > seqNumMax + 1) {
				log.warn("Accepting data even though some data has been missed: offered seq-num {}, my received seq-num {}", sequenceNumber, seqNumMax);
			}

			seqNumMax = sequenceNumber;
			
			return true;
		}

		return false;
	}

	boolean acceptHeartbeat(long sequenceNumber) {
		return seqNumMax < sequenceNumber;
	}

	/**
	 * Asserts liveliness of a writer represented by this WriterProxy. 
	 */
	public void assertLiveliness() {
		livelinessTimestamp = System.currentTimeMillis();
	}
}
