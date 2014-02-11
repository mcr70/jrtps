package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.SequenceNumber;
import net.sf.jrtps.types.SequenceNumberSet;

/**
 * The NackFrag Submessage is used to communicate the state of a Reader to a
 * Writer. When a data change is sent as a series of fragments, the NackFrag
 * Submessage allows the Reader to inform the Writer about specific fragment
 * numbers it is still missing.
 * 
 * see 8.3.7.10 NackFrag, 9.4.5.13 NackFrag Submessage
 * 
 * @author mcr70
 * 
 */
public class NackFrag extends SubMessage {
    public static final int KIND = 0x12;

    private EntityId readerId;
    private EntityId writerId;
    private SequenceNumber writerSN;
    private SequenceNumberSet fragmentNumberState;
    private int count;

    public NackFrag(SubMessageHeader smh, RTPSByteBuffer is) {
        super(smh);

        readMessage(is);
    }

    /**
     * Identifies the Reader entity that requests to receive certain fragments.
     */
    public EntityId getReaderId() {
        return readerId;
    }

    /**
     * Identifies the Writer entity that is the target of the NackFrag message.
     * This is the Writer Entity that is being asked to re-send some fragments.
     */
    public EntityId getWriterId() {
        return writerId;
    }

    /**
     * The sequence number for which some fragments are missing.
     */
    public SequenceNumber getWriterSequenceNumber() {
        return writerSN;
    }

    /**
     * Communicates the state of the reader to the writer. The fragment numbers
     * that appear in the set indicate missing fragments on the reader side. The
     * ones that do not appear in the set are undetermined (could have been
     * received or not).
     */
    public SequenceNumberSet getFragmentNumberState() {
        return fragmentNumberState;
    }

    /**
     * A counter that is incremented each time a new NackFrag message is sent.
     * Provides the means for a Writer to detect duplicate NackFrag messages
     * that can result from the presence of redundant communication paths.
     */
    public int getCount() {
        return count;
    }

    private void readMessage(RTPSByteBuffer bb) {
        this.readerId = EntityId.readEntityId(bb);
        this.writerId = EntityId.readEntityId(bb);
        this.writerSN = new SequenceNumber(bb);
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
