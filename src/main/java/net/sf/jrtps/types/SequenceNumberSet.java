package net.sf.jrtps.types;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * see 9.4.2.6 SequenceNumberSet
 * 
 * @author mcr70
 * 
 */
public class SequenceNumberSet {
    private final SequenceNumber bitmapBase;
    private final int[] bitmaps;
    private final int numBits;

    public SequenceNumberSet(long base, int[] bitmaps) {
        this.bitmapBase = new SequenceNumber(base);
        this.bitmaps = bitmaps;
        this.numBits = bitmaps.length * 32;
    }

    public SequenceNumberSet(RTPSByteBuffer bb) {
        bitmapBase = new SequenceNumber(bb);

        numBits = bb.read_long();
        int count = (numBits + 31) / 32;
        bitmaps = new int[count];

        for (int i = 0; i < bitmaps.length; i++) {
            bitmaps[i] = bb.read_long();
        }
    }

    /**
     * Gets the bitmap base.
     * 
     * @return bitmap base
     */
    public long getBitmapBase() {
        return bitmapBase.getAsLong();
    }

    /**
     * Gets the number of bits in bitmaps.
     * 
     * @return number of bits
     */
    public int getNumBits() {
        return numBits;
    }

    /**
     * Gets the bitmaps as an array of ints.
     * 
     * @return bitmaps
     */
    public int[] getBitmaps() {
        return bitmaps;
    }

    public List<Long> getSequenceNumbers() {
        List<Long> seqNums = new LinkedList<Long>();

        long seqNum = bitmapBase.getAsLong();

        for (int i = 0; i < bitmaps.length; i++) {
            int bitmap = bitmaps[i];

            for (int j = 0; j < 32; j++) {
                if ((bitmap & 0x8000000) == 0x8000000) { // id the MSB matches,
                                                         // add a new seqnum
                    seqNums.add(seqNum);
                }

                seqNum++;
                bitmap = bitmap << 1;
            }
        }

        return seqNums;
    }

    public List<Long> getMissingSequenceNumbers() {
        List<Long> seqNums = new LinkedList<Long>();

        long seqNum = bitmapBase.getAsLong();

        for (int i = 0; i < bitmaps.length; i++) {
            int bitmap = bitmaps[i];

            for (int j = 0; j < 32; j++) {
                if ((bitmap & 0x8000000) == 0x0) { // id the MSB does not
                                                   // matches, add a new seqnum
                    seqNums.add(seqNum);
                }

                seqNum++;
                bitmap = bitmap << 1;
            }
        }

        return seqNums;
    }

    public void writeTo(RTPSByteBuffer buffer) {
        bitmapBase.writeTo(buffer);

        // buffer.write_long(bitmaps.length);
        // buffer.write_long(bitmaps.length * 32);
        buffer.write_long(numBits);
        for (int i = 0; i < bitmaps.length; i++) {
            buffer.write_long(bitmaps[i]);
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(bitmapBase.toString());
        sb.append("/" + numBits);
        sb.append(":[");
        for (int i = 0; i < bitmaps.length; i++) {
            sb.append("0x");
            sb.append(String.format("%04x", bitmaps[i]));

            if (i < bitmaps.length - 1)
                sb.append(' ');
        }
        sb.append(']');

        return sb.toString();
    }
}