package net.sf.jrtps.types;

import java.util.LinkedList;
import java.util.List;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * SequenceNumberSet contains a number of sequence numbers encoded in a bitmap.
 * MSB bit in bitmap represents base of the SequenceNumberSet.
 * 
 * see 9.4.2.6 SequenceNumberSet
 * 
 * @author mcr70
 */
public class SequenceNumberSet {
    private final SequenceNumber bitmapBase;
    private final int[] bitmaps;
    private final int numBits;
	private List<Long> seqNums;
	private List<Long> missingSeqNums;


	public SequenceNumberSet(long base) {
		this.bitmapBase = new SequenceNumber(base);
		this.bitmaps = new int[0];
		this.numBits = 0;
	}
	
	/**
     * Constructor for SequenceNumberSet. 
     * 
     * @param base base of the SequenceNumberSet
     * @param bitmaps Bitmap of the contained sequence numbers in set
     */
    public SequenceNumberSet(long base, int[] bitmaps) {
        this.bitmapBase = new SequenceNumber(base);
        this.bitmaps = bitmaps;
        this.numBits = bitmaps.length * 32;
    }

	/**
     * Constructor for SequenceNumberSet. 
     * 
     * @param base base of the SequenceNumberSet
     * @param numBits Number of bits in bitmaps
     * @param bitmaps Bitmap of the contained sequence numbers in set
     */
    public SequenceNumberSet(long base, int numBits, int[] bitmaps) {
        this.bitmapBase = new SequenceNumber(base);
        this.bitmaps = bitmaps;
        this.numBits = numBits;
    }

    /**
     * Reads a SequenceNumberSet from RTPSByteBuffer.
     * @param bb
     */
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
     * Tests, whether or not given sequenceNumber is contained in this SequenceNumberSet 
     * @param seqNum
     * @return true or false
     */
    public boolean containsSeqNum(long seqNum) {
    	return getSequenceNumbers().contains(seqNum);
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

    /**
     * Gets the sequence numbers set in this SequenceNumberSet.
     * @return a List of sequence numbers
     */
    public List<Long> getSequenceNumbers() {
    	if (seqNums != null) {
    		return seqNums;
    	}
    	
        seqNums = createSeqNumList(0x80000000);
        
        return seqNums;
    }

    /**
     * Gets the sequence numbers missing in this SequenceNumberSet.
     * @return a List of missing sequence numbers
     */
    public List<Long> getMissingSequenceNumbers() {
        if (missingSeqNums != null) {
        	return missingSeqNums;
        }

        missingSeqNums = createSeqNumList(0x0);
        
        return missingSeqNums;
    }
    
    private List<Long> createSeqNumList(int msbValue) {
    	List<Long> snList = new LinkedList<Long>();

        long seqNum = bitmapBase.getAsLong();
        int bitCount = 0;
        
        for (int i = 0; i < bitmaps.length; i++) {
            int bitmap = bitmaps[i];

            for (int j = 0; j < 32 && bitCount < numBits; j++) {
                if ((bitmap & 0x80000000) == msbValue) { // Compare MSB to 0x80000000 or 0x0
                    snList.add(seqNum);
                }

                seqNum++; bitCount++;
                bitmap = bitmap << 1;
            }
        }

        return snList;
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
