package alt.rtps;

import alt.rtps.RTPSWriter.ChangeKind;


class CacheChange {
	private final long sequenceNumber;
	private final Object data;
	private final ChangeKind kind;

	CacheChange(ChangeKind kind, long seqNum, Object data) {
		this.kind = kind;
		sequenceNumber = seqNum;
		this.data = data;
	}

	Object getData() {
		return data;
	}

	long getSequenceNumber() {
		return sequenceNumber;
	}
}
