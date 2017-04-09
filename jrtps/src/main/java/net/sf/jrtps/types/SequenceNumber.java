package net.sf.jrtps.types;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * See 9.4.2.5 SequenceNumber for the mapping.
 * 
 * @author mcr70
 */
public class SequenceNumber {
    public static final SequenceNumber SEQUENCENUMBER_UNKNOWN = new SequenceNumber(-1, 0); // see table 9.4
    
    private int high = 0;
    private int low = 0;

    public SequenceNumber(RTPSByteBuffer bb) {
        this.high = bb.read_long();
        this.low = bb.read_long();
    }

    public SequenceNumber(int high, int low) {
        this.high = high;
        this.low = low;
    }

    public SequenceNumber(long seqNum) {
        low = (int) (seqNum & 0xffffffff);
        high = (int) ((seqNum >> 32) & 0xffffffff);
    }

    public long getAsLong() {
        return ((long)high << 32) | ((long)low & 0xFFFFFFFF);
    }

    /**
     * High bytes, used for testing
     * @return high bytes
     */
    int getHighBytes() {
        return high;
    }
    
    /**
     * low bytes, used for testing
     * @return low bytes
     */
    int getLowBytes() {
        return low;
    }
    
    @Override
    public String toString() {
        return "" + getAsLong();
    }

    public void writeTo(RTPSByteBuffer buffer) {
        buffer.write_long(high);
        buffer.write_long(low);
    }
}
