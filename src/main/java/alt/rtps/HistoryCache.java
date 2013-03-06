package alt.rtps;

import java.util.LinkedList;
import java.util.List;

class HistoryCache {
	private List<CacheChange> changes = new LinkedList<CacheChange>();
	private long seqNumMax = 0;
	private long seqNumMin = 0;
	

	List<CacheChange> getChanges() {
		return changes;
	}

	boolean containsSeqNum(long seqNumToCheck) {
		if (seqNumToCheck >= seqNumMin && seqNumToCheck <= seqNumMax) {
			for (CacheChange change : changes) {		
				if (change.getSequenceNumber() == seqNumToCheck) {
					return true;
				}
			}
		}
		
		// Every change was considered and no match was found
		return false;
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
		if (sequenceNumber == seqNumMax + 1) { 
			changes.add(new CacheChange(sequenceNumber, data));
			seqNumMax++;
			
			return true;
		}

		return false;
	}
	
	long createChange(Object data) {
		seqNumMax++;
		changes.add(new CacheChange(seqNumMax, data));
		
		return seqNumMax;
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
