package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.EntityId_t;
import net.sf.jrtps.types.SequenceNumber_t;

/**
 * This message is sent from an RTPS Writer to an RTPS Reader to communicate 
 * the sequence numbers of changes that the Writer has available.
 * 
 * see 8.3.7.5
 * 
 * @author mcr70
 */
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
	
	Heartbeat(SubMessageHeader smh, RTPSByteBuffer is) {
		super(smh);
		
		readMessage(is);
	}

	/**
	 * Appears in the Submessage header flags. Indicates whether the Reader 
	 * is required to respond to the Heartbeat or if it is just an advisory heartbeat.
	 * If finalFlag is set, Reader is not required to respond with AckNack.
	 * 
	 * @return true if final flag is set
	 */
	public boolean finalFlag() {
		return (header.flags & 0x2) != 0;
	}

	public void finalFlag(boolean flag) {
		if (flag) {
			header.flags |= 0x2;
		}
		else {
			header.flags &= ~0x2;
		}
	}

	/**
	 * Appears in the Submessage header flags. Indicates that the DDS DataWriter 
	 * associated with the RTPS Writer of the message has manually asserted its LIVELINESS.
	 * 
	 * @return true, if liveliness flag is set
	 */
	public boolean livelinessFlag() {
		return (header.flags & 0x4) != 0;
	}
	
	public void livelinessFlag(boolean livelinessFlag) {
		if (livelinessFlag) {
			header.flags |= 0x4;
		}
		else {
			header.flags &= ~0x4;
		}
	}
	/**
	 * Identifies the Reader Entity that is being informed of the availability of a set of sequence numbers.
	 * Can be set to ENTITYID_UNKNOWN to indicate all readers for the writer that sent the message.
	 */
	public EntityId_t getReaderId() {
		return readerId;
	}
	
	/**
	 * Identifies the Writer Entity to which the range of sequence numbers applies.
	 */	
	public EntityId_t getWriterId() {
		return writerId;
	}
	
	/**
	 * Identifies the first (lowest) sequence number that is available in the Writer.
	 */
	public long getFirstSequenceNumber() {
		return firstSN.getAsLong();
	}

	/**
	 * Identifies the last (highest) sequence number that is available in the Writer.
	 */
	public long getLastSequenceNumber() {
		return lastSN.getAsLong();
	}

	/**
	 * A counter that is incremented each time a new Heartbeat message is sent.
	 * Provides the means for a Reader to detect duplicate Heartbeat messages that 
	 * can result from the presence of redundant communication paths.
	 */
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
