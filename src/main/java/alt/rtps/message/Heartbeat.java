package alt.rtps.message;

import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.SequenceNumber_t;

public class Heartbeat extends SubMessage {
	public static final int KIND = 0x07;
	
	private EntityId_t readerId;
	private EntityId_t writerId;
	private SequenceNumber_t firstSN;
	private SequenceNumber_t lastSN;
	private int count;

	public Heartbeat(EntityId_t readerId, EntityId_t writerId, 
			long firstSeqNum, long lastSeqNum, int count) {
		super(new SubMessageHeader(KIND));
		
		this.readerId = readerId;
		this.writerId = writerId;
		this.count = count;
		firstSN = new SequenceNumber_t(firstSeqNum);
		lastSN = new SequenceNumber_t(lastSeqNum);

		header.flags |= 2; // set FinalFlag. No response needed.
	}
	
	public Heartbeat(SubMessageHeader smh, RTPSByteBuffer is) {
		super(smh);
		
		readMessage(is);
	}

	public boolean finalFlag() {
		return (header.flags & 0x2) != 0;
	}

	public boolean livelinessFlag() {
		return (header.flags & 0x4) != 0;
	}
	
	public EntityId_t getReaderId() {
		return readerId;
	}
	
	public EntityId_t getWriterId() {
		return writerId;
	}
	
	public SequenceNumber_t getFirstSequenceNumber() {
		return firstSN;
	}

	public SequenceNumber_t getLastSequenceNumber() {
		return lastSN;
	}

	public int getCount() {
		return count;
	}


	
	private void readMessage(RTPSByteBuffer is) {
		this.readerId = EntityId_t.readEntityId(is);
		this.writerId = EntityId_t.readEntityId(is);
		this.firstSN = new SequenceNumber_t(is);
		this.lastSN = new SequenceNumber_t(is);
		
		this.count = is.read_long();
	}

	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		readerId.writeTo(buffer);
		writerId.writeTo(buffer);
		firstSN.writeTo(buffer);
		lastSN.writeTo(buffer);
		
		buffer.write_long(count);
	}

	public String toString() {
		return super.toString() + ", " + readerId + ", " + writerId + ", " + 
				firstSN + ", " + lastSN + ", finalFlag=" + finalFlag() + 
				", livelinessFlag=" + livelinessFlag();
	}
}
