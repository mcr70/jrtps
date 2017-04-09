package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * A Header of the SubMessage. see 8.3.3.2 Submessage structure
 * 
 * @author mcr70
 * 
 */
public class SubMessageHeader {
    /**
     * Default value for endianness in sub messages.
     */
    private static final byte DEFAULT_ENDIANNESS_FLAG = 0x00;

    byte kind;
    byte flags; // 8 flags
    int submessageLength; // ushort

    /**
     * Constructs this SubMessageHeader with given kind and
     * DEFAULT_ENDIANESS_FLAG. Length of the SubMessage is set to 0. Length will
     * be calculated during marshalling of the Message.
     * 
     * @param kind Kind of SubMessage
     */
    public SubMessageHeader(int kind) {
        this(kind, DEFAULT_ENDIANNESS_FLAG);
    }

    /**
     * Constructs this SubMessageHeader with given kind and flags. Length of the
     * SubMessage is set to 0. Length will be calculated during marshalling of
     * the Message.
     * 
     * @param kind Kind of SubMessage
     * @param flags flags of this SubMessageHeader
     */
    public SubMessageHeader(int kind, int flags) {
        this.kind = (byte) kind;
        this.flags = (byte) flags;
        this.submessageLength = 0; // Length will be calculated during
                                   // Data.writeTo(...);
    }

    /**
     * Constructs SubMessageHeader by reading from RTPSByteBuffer.
     * 
     * @param bb
     */
    SubMessageHeader(RTPSByteBuffer bb) {
        kind = (byte) bb.read_octet();
        flags = (byte) bb.read_octet();
        bb.setEndianess(endiannessFlag());

        submessageLength = ((int) bb.read_short()) & 0xffff;
    }

    /**
     * Writes this SubMessageHeader into RTPSByteBuffer
     * 
     * @param bb RTPSByteBuffer to write to
     */
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_octet(kind);
        bb.write_octet(flags);
        bb.write_short((short) submessageLength);
    }

    /**
     * Get the endianness for SubMessage. If endianness flag is set,
     * little-endian is used by SubMessage, otherwise big-endian is used.
     * 
     * @return true, if endianness flag is set
     */
    public boolean endiannessFlag() {
        return (flags & 0x1) == 0x1;
    }

    /**
     * Get the length of the sub message.
     * 
     * @return length of the sub message
     */
    public int getSubMessageLength() {
        return submessageLength;
    }

    /**
     * Get the kind of SubMessage
     * 
     * @return kind
     */
    public byte getSubMessageKind() {
        return kind;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("header[");
        sb.append("0x");
        sb.append(String.format("%02x", kind));
        sb.append(",0x");
        sb.append(String.format("%02x", flags));
        sb.append(',');
        sb.append(((int) submessageLength) & 0xffff);
        sb.append(']');

        return sb.toString();
    }
}
