package net.sf.jrtps;



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
}
