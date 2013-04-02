package alt.rtps;

import java.util.LinkedList;
import java.util.List;

import alt.rtps.types.GUID_t;

class HistoryCache {
	private List<CacheChange> changes = new LinkedList<CacheChange>();
	private GUID_t guid; // HistoryCache belongs to a specific GUID_t
	
	private volatile long seqNumMax = 0;
	private volatile long seqNumMin = 0;
	
	private int maxSize;
	
	public HistoryCache(GUID_t guid) {
		this(guid, Integer.MAX_VALUE);
	}
	
	public HistoryCache(GUID_t guid, int maxSize) {
		this.guid = guid;
		this.maxSize = maxSize;
	}

	List<CacheChange> getChanges() {
		return changes; 
	}

	/**
	 * Get the Guid_t that this history cache belongs to
	 * @return
	 */
	GUID_t getGuid() {
		return guid;
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
	boolean createChange(Object data, long sequenceNumber) {
		// Data must come in order. If not, drop it. Manage out-of-order data with 
		// HeartBeat & AckNack messages
		
		synchronized (changes) {
			if (changes.size() >= maxSize) {
				changes.remove(0);
			}
		}

		if (true || sequenceNumber == seqNumMax + 1) { 
			changes.add(new CacheChange(sequenceNumber, data));
			seqNumMax++;
			
			return true;
		}

		return false;
	}
	
	public boolean createChange(Object data) {
		return createChange(data, seqNumMax + 1);
	}

	long getSeqNumMin() {
		if (seqNumMin == 0) { // 0 means not set. 
			CacheChange cc = changes.get(0); // Get first one. TODO: IndexOutOfBounds
			seqNumMin = cc.getSequenceNumber();
		}
		
		return seqNumMin;
	}

	int size() {
		return changes.size();
	}
}
