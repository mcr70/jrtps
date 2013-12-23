package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.SequenceNumberSet;
import net.sf.jrtps.types.SequenceNumber;

/**
 * This Submessage is sent from an RTPS Writer to an RTPS Reader and indicates to the RTPS Reader 
 * that a range of sequence numbers is no longer relevant. The set may be a contiguous range of 
 * sequence numbers or a specific set of sequence numbers.<p>
 * 
 * see 8.3.7.4 Gap, 9.4.5.5 Gap Submessage
 * 
 * @author mcr70
 * 
 */
public class Gap extends SubMessage {
	public static final int KIND = 0x08;
	
	/**
	 * Identifies the Reader Entity that is being informed of the irrelevance of a set of sequence numbers.
	 */
	private EntityId readerId;
	/**
	 * Identifies the Writer Entity to which the range of sequence numbers applies.
	 */
	private EntityId writerId;
	/**
	 * Identifies the first sequence number in the interval of irrelevant sequence numbers.
	 */
	private SequenceNumber gapStart;
	/**
	 * Serves two purposes:
	 * (1) Identifies the last sequence number in the interval of irrelevant sequence numbers.
	 * (2) Identifies an additional list of sequence numbers that are irrelevant.
	 */
	private SequenceNumberSet gapList;

	public Gap(SubMessageHeader smh, RTPSByteBuffer bb) {
		super(smh);
		
		readMessage(bb);
	}

	public EntityId getReaderId() {
		return readerId;
	}
	
	public EntityId getWriterId() {
		return writerId;
	}

	public SequenceNumber getGapStart() {
		return gapStart;
	}
	
	public SequenceNumberSet getGapList() {
		return gapList;
	}
	
	
	private void readMessage(RTPSByteBuffer bb) {
		this.readerId = EntityId.readEntityId(bb);
		this.writerId = EntityId.readEntityId(bb);
		
		this.gapStart = new SequenceNumber(bb);
		this.gapList = new SequenceNumberSet(bb);
	}


	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		readerId.writeTo(buffer);
		writerId.writeTo(buffer);
		gapStart.writeTo(buffer);
		gapList.writeTo(buffer);
	}
}
