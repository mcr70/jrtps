package alt.rtps.message;

import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.EntityId_t;
import alt.rtps.types.SequenceNumber_t;

/**
 * When fragmenting data and until all fragments are available, the HeartbeatFrag Submessage is sent 
 * from an RTPS Writer to an RTPS Reader to communicate which fragments the Writer has available. 
 * This enables reliable communication at the fragment level.<br>
 * 
 * Once all fragments are available, a regular Heartbeat message is used.
 * 
 * @author mcr70
 * @see 9.4.5.7 HeartBeatFrag Submessage
 */
public class HeartbeatFrag extends SubMessage {
	public static final int KIND = 0x13;
	
	/**
	 * Identifies the Reader Entity that is being informed of the availability
	 * of fragments. Can be set to ENTITYID_UNKNOWN to indicate all
	 * readers for the writer that sent the message.
	 */
	private EntityId_t readerId;
	/**
	 * Identifies the Writer Entity that sent the Submessage.
	 */
	private EntityId_t writerId;
	/**
	 * Identifies the sequence number of the data change for which fragments are available.
	 */
	private SequenceNumber_t writerSN;
	/**
	 * All fragments up to and including this last (highest) fragment are
	 * available on the Writer for the change identified by writerSN.
	 */
	private int lastFragmentNum;
	/**
	 * A counter that is incremented each time a new HeartbeatFrag message
	 * is sent. Provides the means for a Reader to detect duplicate
	 * HeartbeatFrag messages that can result from the presence of
	 * redundant communication paths.
	 */
	private int count;

	public HeartbeatFrag(SubMessageHeader smh, RTPSByteBuffer bb) {
		super(smh);
		
		readMessage(bb);
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

	public int getLastFragmentNumber() {
		return lastFragmentNum;
	}

	public int getCount() {
		return count;
	}

	
	private void readMessage(RTPSByteBuffer bb) {
		this.readerId = EntityId_t.readEntityId(bb);
		this.writerId = EntityId_t.readEntityId(bb);
		this.writerSN = new SequenceNumber_t(bb);
		this.lastFragmentNum = bb.read_long(); // ulong
		this.count = bb.read_long();
	}


	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		readerId.writeTo(buffer);
		writerId.writeTo(buffer);
		writerSN.writeTo(buffer);
		
		buffer.write_long(lastFragmentNum);
		buffer.write_long(count);
	}
}
