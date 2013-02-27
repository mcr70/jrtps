package alt.rtps.message;

import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.SequenceNumberSet;

/**
 * This Submessage is used to communicate the state of a Reader to a Writer. The Submessage allows 
 * the Reader to inform the Writer about the sequence numbers it has received and which ones it is 
 * still missing. This Submessage can be used to do both positive and negative acknowledgments.
 * 
 * @author mcr70
 * @see 8.3.7.1 AckNack
 */
public class AckNack extends SubMessage {
	public static final int KIND = 0x06;
	
	/**
	 * Identifies the Reader entity that acknowledges receipt of certain
	 * sequence numbers and/or requests to receive certain sequence numbers.
	 */
	private EntityId_t readerId;
	/**
	 * Identifies the Writer entity that is the target of the AckNack
	 * message. This is the Writer Entity that is being asked to re-send
	 * some sequence numbers or is being informed of the reception of
	 * certain sequence numbers.
	 */
	private EntityId_t writerId;
	/**
	 * Communicates the state of the reader to the writer.
	 * All sequence numbers up to the one prior to readerSNState.base
	 * are confirmed as received by the reader.
	 * The sequence numbers that appear in the set indicate missing
	 * sequence numbers on the reader side. The ones that do not
	 * appear in the set are undetermined (could be received or not).
	 */
	private SequenceNumberSet readerSNState;
	/**
	 * A counter that is incremented each time a new AckNack message is sent.
	 * Provides the means for a Writer to detect duplicate AckNack messages that can result from 
	 * the presence of redundant communication paths.
	 */
	private int count;
	
	public AckNack(EntityId_t readerId, EntityId_t writerId, SequenceNumberSet readerSnSet, int count) {
		super(new SubMessageHeader(KIND));
		
		this.readerId = readerId;
		this.writerId = writerId;
		this.readerSNState = readerSnSet;
		this.count = count;
	}
	
	
	public AckNack(SubMessageHeader smh, RTPSByteBuffer bb) {
		super(smh);
		readMessage(bb);
	}

	public boolean finalFlag() {
		return (header.flags & 0x2) != 0;
	}
	
	public EntityId_t getReaderId() {
		return readerId;
	}
	
	public EntityId_t getWriterId() {
		return writerId;
	}
	
	public SequenceNumberSet getReaderSNState() {
		return readerSNState;
	}
	
	public int getCount() {
		return count;
	}

	
	private void readMessage(RTPSByteBuffer is) {
		this.readerId = EntityId_t.readEntityId(is);
		this.writerId = EntityId_t.readEntityId(is);
		this.readerSNState = new SequenceNumberSet(is);
		this.count = is.read_long();
	}


	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		readerId.writeTo(buffer);
		writerId.writeTo(buffer);
		readerSNState.writeTo(buffer);
		buffer.write_long(count);
	}


	public String toString() {
		return super.toString() + ", " + readerId + ", " + writerId + ", " + readerSNState + ", " + count;
	}
}
