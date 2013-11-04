package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * A Header of the SubMessage.
 * 
 * @author mcr70
 *
 */
public class SubMessageHeader {
	/**
	 * Default value for endianess in sub messages. extending classes should utilize this
	 * value.
	 */
	public static final byte DEFAULT_ENDIANESS_FLAG = 0x01;

	byte kind;
	byte flags; // 8 flags
	int submessageLength; // ushort

	/**
	 * Constructs this SubMessageHeader with given kind and DEFAULT_ENDIANESS_FLAG.
	 * Length of the SubMessage is set to 0. Length will be calculated during marshalling
	 * of the Message.
	 * @param kind
	 */
	public SubMessageHeader(int kind) {
		this(kind, DEFAULT_ENDIANESS_FLAG);
	}

	/**
	 * Constructs this SubMessageHeader with given kind and flags.
	 * Length of the SubMessage is set to 0. Length will be calculated during marshalling
	 * of the Message.
	 *
	 * @param kind
	 * @param flags
	 */
	public SubMessageHeader(int kind, int flags) {
		this.kind = (byte) kind;
		this.flags = (byte) flags;
		this.submessageLength = 0; // Length will be calculated during Data.writeTo(...);		
	}
	
	SubMessageHeader(RTPSByteBuffer bb) {
		kind = (byte) bb.read_octet();
		flags = (byte) bb.read_octet();
		bb.setEndianess(endianessFlag());
		
		submessageLength = ((int)bb.read_short()) & 0xffff;
	}


	/**
	 * Writes this SubMessageHeader into RTPSByteBuffer
	 * @param buffer
	 */
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_octet(kind);
		buffer.write_octet(flags);
		buffer.write_short((short) submessageLength);
	}
	/**
	 * Get the endianness for SubMessage. If endianness flag is set, big-endian is used 
	 * by SubMessage, otherwise little-endian is used.
	 * 
	 * @return true, if endianness flag is set
	 */
	public boolean endianessFlag() {
		return (flags & 0x1) == 0x1;
	}
	
	public boolean isBigEndian() {
		return !endianessFlag();
	}
	
	/**
	 * Get the length of the sub message.
	 * @return length of the sub message
	 */
	public int getSubMessageLength() {
		return submessageLength;
	}

	/**
	 * Get the kind of SubMessage
	 * @return kind
	 */
	public byte getSubMessageKind() {
		return kind;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer("header[");
		sb.append("0x");
		sb.append(String.format("%02x", kind));
		sb.append(",0x");
		sb.append(String.format("%02x", flags));
		sb.append(',');
		sb.append(((int)submessageLength) & 0xffff);
		sb.append(']');
		
		return sb.toString();
	}
}
