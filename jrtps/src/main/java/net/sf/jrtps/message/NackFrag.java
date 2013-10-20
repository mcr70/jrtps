package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.EntityId_t;
import net.sf.jrtps.types.SequenceNumberSet;
import net.sf.jrtps.types.SequenceNumber_t;

/**
 * The NackFrag Submessage is used to communicate the state of a Reader to a Writer. When a data change
 * is sent as a series of fragments, the NackFrag Submessage allows the Reader to inform the Writer 
 * about specific fragment numbers it is still missing.
 *
 * @author mcr70
 * @see 8.3.7.10 NackFrag, 9.4.5.13 NackFrag Submessage
 */
public class NackFrag extends SubMessage {
	public static final int KIND = 0x12;
	
	/**
	 * Identifies the Reader entity that requests to receive certain fragments.
	 */
	private EntityId_t readerId;
	/**
	 * Identifies the Writer entity that is the target of the NackFrag message.
	 * This is the Writer Entity that is being asked to re-send some fragments.
	 */
	private EntityId_t writerId;
	/**
	 * The sequence number for which some fragments are missing.
	 */
	private SequenceNumber_t writerSN;
	/**
	 * Communicates the state of the reader to the writer. The fragment numbers that appear in the set 
	 * indicate missing fragments on the reader side. The ones that do not appear in the set
	 * are undetermined (could have been received or not).
	 */
	private SequenceNumberSet fragmentNumberState;
	/**
	 * A counter that is incremented each time a new NackFrag message is sent.
	 * Provides the means for a Writer to detect duplicate NackFrag
	 * messages that can result from the presence of redundant
	 * communication paths.
	 */
	private int count;

	public NackFrag(SubMessageHeader smh, RTPSByteBuffer is) {
		super(smh);
		
		readMessage(is);
	}

	public EntityId_t getReaderId() {
		return readerId;
	}
	
	public EntityId_t getWriterId() {
		return writerId;
	}
	
	public SequenceNumber_t getWriterSequenceNumber() {
		return writerSN;
	}

	public SequenceNumberSet getFragmentNumberState() {
		return fragmentNumberState;
	}
	
	public int getCount() {
		return count;
	}

	
	private void readMessage(RTPSByteBuffer bb) {
		this.readerId = EntityId_t.readEntityId(bb);
		this.writerId = EntityId_t.readEntityId(bb);
		this.writerSN = new SequenceNumber_t(bb);
		this.fragmentNumberState = new SequenceNumberSet(bb);
		
		this.count = bb.read_long();
	}


	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		readerId.writeTo(buffer);
		writerId.writeTo(buffer);
		writerSN.writeTo(buffer);
		fragmentNumberState.writeTo(buffer);
		
		buffer.write_long(count);
	}
}
