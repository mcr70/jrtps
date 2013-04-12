package alt.rtps;

import alt.rtps.types.GUID_t;


/**
 * WriterProxy represents a remote writer
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
	 * 
	 * @param data
	 * @param sequenceNumber
	 * @return true, if data was added to cache
	 */
	boolean acceptData(Object data, long sequenceNumber) {
		// Data must come in order. If not, drop it. Manage out-of-order data with 
		// HeartBeat & AckNack messages

		if (true || sequenceNumber == seqNumMax + 1) { 
			//changes.add(new CacheChange(sequenceNumber, data));
			seqNumMax++;
			
			return true;
		}

		return false;
	}
}
