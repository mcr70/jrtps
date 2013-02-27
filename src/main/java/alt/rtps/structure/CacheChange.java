package alt.rtps.structure;


public class CacheChange {
	private long sequenceNumber;
	private Object data;

	public CacheChange(long seqNum, Object data) {
		this.setSequenceNumber(seqNum);
		this.data = data;
	}

	public Object getData() {
		return data;
	}

	public long getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(long sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
}
