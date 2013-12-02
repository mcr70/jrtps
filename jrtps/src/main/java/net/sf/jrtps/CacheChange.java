package net.sf.jrtps;


/**
 * This class represents a sample in history cache.
 * 
 * @author mcr70
 */
class CacheChange implements Comparable<CacheChange> {
	private final long sequenceNumber;
	private final Object data;
	private final ChangeKind kind;
	private final long timeStamp;
	private final int hashCode;
	
	CacheChange(ChangeKind kind, long seqNum, Object data) {
		this.kind = kind;
		this.sequenceNumber = seqNum;
		this.data = data;
		this.timeStamp = System.currentTimeMillis(); // NOTE: write_w_timestamp
		this.hashCode = new Long(sequenceNumber).hashCode();
	}

	Object getData() {
		return data;
	}

	long getSequenceNumber() {
		return sequenceNumber;
	}
	
	ChangeKind getKind() {
		return kind;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	// TODO: should we implement equals() and hashCode(). If so, it would be for sequenceNumber
	@Override
	public boolean equals(Object other) {
		if (other instanceof CacheChange) {
			return sequenceNumber == ((CacheChange)other).sequenceNumber;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return hashCode;
	}
	
	@Override
	public int compareTo(CacheChange o) {
		return (int) (sequenceNumber - o.sequenceNumber);
	}
	
	public String toString() {
		return "change: " + sequenceNumber;
	}
}
