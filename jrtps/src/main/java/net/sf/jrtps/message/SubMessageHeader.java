package net.sf.jrtps.message;

import net.sf.jrtps.transport.RTPSByteBuffer;

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



	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_octet(kind);
		buffer.write_octet(flags);
		buffer.write_short((short) submessageLength);
	}

	public boolean endianessFlag() {
		return (flags & 0x1) == 0x1;
	}
	
	public boolean isBigEndian() {
		return !endianessFlag();
	}
	
	public int getSubMessageLength() {
		return submessageLength;
	}

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
