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
public class FragmentNumberSet {
    // TODO: this class is identical to SequenceNumberSet, except for bitmapBase
    private final FragmentNumber bitmapBase;
    private final int[] bitmaps;
    private final int numBits;

    public FragmentNumberSet(int base, int[] bitmaps) {
        this.bitmapBase = new FragmentNumber(base);
        this.bitmaps = bitmaps;
        this.numBits = bitmaps.length * 32;
    }

    public FragmentNumberSet(RTPSByteBuffer bb) {
        bitmapBase = new FragmentNumber(bb);

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
    public int getBitmapBase() {
        return bitmapBase.getValue();
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

    /**
     * Gets the sequence numbers set in this SequenceNumberSet.
     * @return a List of sequence numbers
     */
    public List<Integer> getSequenceNumbers() {
        List<Integer> seqNums = new LinkedList<Integer>();

        int seqNum = bitmapBase.getValue();
        int bitCount = 0;
        
        for (int i = 0; i < bitmaps.length; i++) {
            int bitmap = bitmaps[i];

            for (int j = 0; j < 32 && bitCount < numBits; j++) {
                if ((bitmap & 0x8000000) == 0x8000000) { // id the MSB matches, add a new seqnum
                    seqNums.add(seqNum);
                }

                seqNum++; bitCount++;
                bitmap = bitmap << 1;
            }
        }

        return seqNums;
    }

    /**
     * Gets the sequence numbers missing in this SequenceNumberSet.
     * @return a List of missing sequence numbers
     */
    public List<Integer> getMissingSequenceNumbers() {
        List<Integer> seqNums = new LinkedList<Integer>();

        int seqNum = bitmapBase.getValue();

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
