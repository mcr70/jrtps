package alt.rtps;

import alt.rtps.types.GUID_t;


/**
 * WriterProxy represents a remote writer. It also determines if
 * an incoming Data message is out-of-order or not.
 * 
 * @author mcr70
 *
 */
class WriterProxy {
	private final GUID_t writerGuid;
	
	private volatile long seqNumMax = 0;
	
	
	public WriterProxy(GUID_t writerGuid) {
		this.writerGuid = writerGuid;
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

		if (true || sequenceNumber == seqNumMax + 1) { 
			seqNumMax++;
			
			return true;
		}

		return false;
	}
}
