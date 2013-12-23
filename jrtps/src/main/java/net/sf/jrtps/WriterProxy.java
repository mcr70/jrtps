package net.sf.jrtps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.jrtps.builtin.WriterData;
import net.sf.jrtps.types.Guid;


/**
 * WriterProxy represents a remote writer. It also determines if
 * an incoming Data message is out-of-order or not.
 * 
 * @author mcr70
 *
 */
class WriterProxy {
	private static final Logger log = LoggerFactory.getLogger(WriterProxy.class);
	private final Guid writerGuid;
	private WriterData wd;
	
	private volatile long seqNumMax = 0;

	
	
	WriterProxy(WriterData wd) {
		this.wd = wd;
		writerGuid = wd.getKey();
	}
	
	WriterProxy(Guid writerGuid) {
		this.writerGuid = writerGuid;
	}
	
	Guid getGuid() {
		return writerGuid;
	}

	long getSeqNumMax() {
		return seqNumMax;
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
}
