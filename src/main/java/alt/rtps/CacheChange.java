package alt.rtps;


class CacheChange {
	private long sequenceNumber;
	private Object data;

	CacheChange(long seqNum, Object data) {
		this.setSequenceNumber(seqNum);
		this.data = data;
	}

	Object getData() {
		return data;
	}

	long getSequenceNumber() {
		return sequenceNumber;
	}

	void setSequenceNumber(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
}
