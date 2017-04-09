package net.sf.jrtps.types;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * See 9.4.2.7 for the mapping of fragment number.
 * 
 * @author mcr70
 */
public class FragmentNumber {
    private int value;
    
    /**
     * Reads a FragmentNumber from given RTPSByteBuffer.
     * @param bb RTPSByteBuffer
     */
    public FragmentNumber(RTPSByteBuffer bb) {
        this.value = bb.read_long();
    }
    
    /**
     * Creates a new FragmentNumber with given value.
     * @param value a value
     */
    public FragmentNumber(int value) {
        this.value = value;
    }
    
    /**
     * Gets the value of this FragmentNumber
     * @return value
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Writes this FragmentNumber to given RTPSByteBuffer.
     * @param bb RTPSByteBuffer
     */
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(value);
    }

    public String toString() {
        return "" + value;
    }
}
