package net.sf.jrtps.types;

import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * Every Participant has GUID (prefix, ENTITYID_PARTICIPANT), where the constant
 * ENTITYID_PARTICIPANT is a special value defined by the RTPS protocol. Its
 * actual value depends on the PSM. The implementation is free to choose the
 * prefix, as long as every Participant in the Domain has a unique GUID.
 * <p>
 * see 8.2.4.2 The GUIDs of RTPS Participants<br>
 * see 9.3.1.1 Mapping of the GuidPrefix_t
 * 
 * @author mcr70
 */
public class GuidPrefix {
    public static final GuidPrefix GUIDPREFIX_UNKNOWN = new GuidPrefix(
            new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
    /**
     * GuidPrefix used with SecureSubMessage
     */
    public static final GuidPrefix GUIDPREFIX_SECURED = new GuidPrefix(
            new byte[] { (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, 
            		(byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff });

    /**
     * bytes must be of length 12
     */
    private final byte[] bytes;

    public GuidPrefix(RTPSByteBuffer bb) {
        bytes = new byte[12];
        bb.read(bytes);
    }

    /**
     * Create GuidPrefix with given byte array. The length of the array must be 12.
     * 
     * @param bytes bytes of the GuidPrefix
     * @throws IllegalArgumentException if length of the array is not 12
     */
    public GuidPrefix(byte[] bytes) {
        this.bytes = bytes;

        if (bytes.length != 12) {
            throw new IllegalArgumentException("Length of GuidPrefix must be 12");
        }
    }

    /**
     * Gets the bytes of this GuidPrefix
     * 
     * @return byte array of length 12
     */
    public byte[] getBytes() {
        return bytes;
    }

    public String toString() {
        return Arrays.toString(bytes);
    }

    public void writeTo(RTPSByteBuffer buffer) {
        buffer.write(bytes);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof GuidPrefix) {
            GuidPrefix other = (GuidPrefix) o;

            return Arrays.equals(bytes, other.bytes);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }
}
