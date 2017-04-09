package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.SequenceNumber;

/**
 * When fragmenting data and until all fragments are available, the
 * HeartbeatFrag Submessage is sent from an RTPS Writer to an RTPS Reader to
 * communicate which fragments the Writer has available. This enables reliable
 * communication at the fragment level.<br>
 * 
 * Once all fragments are available, a regular Heartbeat message is used.
 * 
 * see 9.4.5.7 HeartBeatFrag Submessage
 * 
 * @author mcr70
 * 
 */
public class HeartbeatFrag extends SubMessage {
    public static final int KIND = 0x13;

    /**
     * Identifies the Reader Entity that is being informed of the availability
     * of fragments. Can be set to ENTITYID_UNKNOWN to indicate all readers for
     * the writer that sent the message.
     */
    private EntityId readerId;
    /**
     * Identifies the Writer Entity that sent the Submessage.
     */
    private EntityId writerId;
    /**
     * Identifies the sequence number of the data change for which fragments are
     * available.
     */
    private SequenceNumber writerSN;
    /**
     * All fragments up to and including this last (highest) fragment are
     * available on the Writer for the change identified by writerSN.
     */
    private int lastFragmentNum;
    /**
     * A counter that is incremented each time a new HeartbeatFrag message is
     * sent. Provides the means for a Reader to detect duplicate HeartbeatFrag
     * messages that can result from the presence of redundant communication
     * paths.
     */
    private int count;

    public HeartbeatFrag(SubMessageHeader smh, RTPSByteBuffer bb) {
        super(smh);

        readMessage(bb);
    }

    public EntityId getReaderId() {
        return readerId;
    }

    public EntityId getWriterId() {
        return writerId;
    }

    public SequenceNumber getWriterSequenceNumber() {
        return writerSN;
    }

    public int getLastFragmentNumber() {
        return lastFragmentNum;
    }

    public int getCount() {
        return count;
    }

    private void readMessage(RTPSByteBuffer bb) {
        this.readerId = EntityId.readEntityId(bb);
        this.writerId = EntityId.readEntityId(bb);
        this.writerSN = new SequenceNumber(bb);
        this.lastFragmentNum = bb.read_long(); // ulong
        this.count = bb.read_long();
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        readerId.writeTo(bb);
        writerId.writeTo(bb);
        writerSN.writeTo(bb);

        bb.write_long(lastFragmentNum);
        bb.write_long(count);
    }
}
