package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.EntityId;
import net.sf.jrtps.types.SequenceNumberSet;

/**
 * This Submessage is used to communicate the state of a Reader to a Writer. The
 * Submessage allows the Reader to inform the Writer about the sequence numbers
 * it has received and which ones it is still missing. This Submessage can be
 * used to do both positive and negative acknowledgments.
 * <p>
 * 
 * see 8.3.7.1 AckNack
 * 
 * @author mcr70
 * 
 */
public class AckNack extends SubMessage {
    public static final int KIND = 0x06;

    private EntityId readerId;
    private EntityId writerId;
    private SequenceNumberSet readerSNState;
    private int count;

    public AckNack(EntityId readerId, EntityId writerId, SequenceNumberSet readerSnSet, int count) {
        super(new SubMessageHeader(KIND));

        this.readerId = readerId;
        this.writerId = writerId;
        this.readerSNState = readerSnSet;
        this.count = count;
    }

    AckNack(SubMessageHeader smh, RTPSByteBuffer bb) {
        super(smh);
        readMessage(bb);
    }

    /**
     * Final flag indicates to the Writer whether a response is mandatory.
     * 
     * @return true, if response is NOT mandatory
     */
    public boolean finalFlag() {
        return (header.flags & 0x2) != 0;
    }

    /**
     * Sets the finalFlag.
     * 
     * @param value
     */
    public void finalFlag(boolean value) {
        if (value) {
            header.flags |= 0x2;
        } else {
            header.flags &= ~0x2;
        }
    }

    /**
     * Identifies the Reader entity that acknowledges receipt of certain
     * sequence numbers and/or requests to receive certain sequence numbers.
     */
    public EntityId getReaderId() {
        return readerId;
    }

    /**
     * Identifies the Writer entity that is the target of the AckNack message.
     * This is the Writer Entity that is being asked to re-send some sequence
     * numbers or is being informed of the reception of certain sequence
     * numbers.
     */
    public EntityId getWriterId() {
        return writerId;
    }

    /**
     * Communicates the state of the reader to the writer. All sequence numbers
     * up to the one prior to readerSNState.base are confirmed as received by
     * the reader. The sequence numbers that appear in the set indicate missing
     * sequence numbers on the reader side. The ones that do not appear in the
     * set are undetermined (could be received or not).
     */
    public SequenceNumberSet getReaderSNState() {
        return readerSNState;
    }

    /**
     * A counter that is incremented each time a new AckNack message is sent.
     * Provides the means for a Writer to detect duplicate AckNack messages that
     * can result from the presence of redundant communication paths.
     */
    public int getCount() {
        return count;
    }

    private void readMessage(RTPSByteBuffer is) {
        this.readerId = EntityId.readEntityId(is);
        this.writerId = EntityId.readEntityId(is);
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
        return super.toString() + " #" + count + ", " + readerId + ", " + writerId + ", " + readerSNState + ", F:"
                + finalFlag();
    }
}
