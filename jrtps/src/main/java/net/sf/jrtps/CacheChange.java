package net.sf.jrtps;

import net.sf.jrtps.RTPSWriter.ChangeKind;

/**
 * Represents a history-cache entry.
 * 
 * @author mcr70
 *
 */
class CacheChange {
	private final long sequenceNumber;
	private final Object data;
	private final ChangeKind kind;
	private final long timeStamp;
	
	CacheChange(ChangeKind kind, long seqNum, Object data) {
		this.kind = kind;
		this.sequenceNumber = seqNum;
		this.data = data;
		this.timeStamp = System.currentTimeMillis();
	}

	/**
	 * Get the data associated with this CacheChange.
	 * @return
	 */
	Object getData() {
		return data;
	}

	/**
	 * Gets the sequence number of this CacheChange.
	 * @return
	 */
	long getSequenceNumber() {
		return sequenceNumber;
	}
	
	/**
	 * Gets the timestamp this CacheChange was created.
	 * @return
	 */
	long getTimeStamp() {
		return timeStamp;
	}

	/**
	 * Get the ChangeKind of this CacheChange.
	 * @return
	 */
	ChangeKind getKind() {
		return kind;
	}
}
