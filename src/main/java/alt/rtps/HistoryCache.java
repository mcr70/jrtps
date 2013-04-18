package alt.rtps;

import java.util.LinkedList;
import java.util.List;

import alt.rtps.types.GUID_t;

class HistoryCache {
	enum ChangeKind {
		WRITE, DISPOSE, UNREGISTER;
	}

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


	public boolean createChange(Object data) {
		return createChange(ChangeKind.WRITE, data);
	}

	public boolean createChange(ChangeKind kind, Object data) {

		synchronized (changes) {
			if (changes.size() >= maxSize) {
				changes.remove(0);
			}
		}

		changes.add(new CacheChange(kind, ++seqNumMax, data));

		return true;
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
