package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Time;

/**
 * Provides a source timestamp for subsequent Entity Submessages. In order to
 * implement the DDS_BY_SOURCE_TIMESTAMP_DESTINATIONORDER_QOS policy,
 * implementations must include an InfoTimestamp Submessage with every update
 * from a Writer.
 * 
 * see 8.3.7.9.6 InfoTimestamp
 * 
 * @author mcr70
 * 
 */
public class InfoTimestamp extends SubMessage {
    public static final int KIND = 0x09;

    /**
     * Present only if the InvalidateFlag is not set in the header. Contains the
     * timestamp that should be used to interpret the subsequent Submessages.
     */
    private Time timestamp;

    public InfoTimestamp(SubMessageHeader smh, RTPSByteBuffer bb) {
        super(smh);

        readMessage(bb);
    }

    public InfoTimestamp(long systemCurrentMillis) {
        super(new SubMessageHeader(KIND));
        this.timestamp = new Time(systemCurrentMillis);
    }

    /**
     * Indicates whether subsequent Submessages should be considered as having a
     * timestamp or not. Timestamp is present in _this_ submessage only if the
     * InvalidateFlag is not set in the header.
     * 
     * @return true, if invalidateFlag is set
     */
    public boolean invalidateFlag() {
        return (header.flags & 0x2) != 0;
    }

    private void readMessage(RTPSByteBuffer bb) {
        if (!invalidateFlag()) {
            this.timestamp = new Time(bb);
        }
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        if (!invalidateFlag()) {
            timestamp.writeTo(bb);
        }
    }

    /**
     * Gets the timestamp
     * 
     * @return Time
     */
    public Time getTimeStamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return super.toString() + ", " + timestamp;
    }
}
