package net.sf.jrtps.types;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * see 9.3.2 Mapping of the Types that Appear Within Submessages or Built-in Topic Data
 * @author mcr70
 * 
 */
public class ProtocolVersion_t {
	public static final int LENGTH = 2;
	public static final ProtocolVersion_t PROTOCOLVERSION_1_0 = new ProtocolVersion_t(1, 0);
	public static final ProtocolVersion_t PROTOCOLVERSION_1_1 = new ProtocolVersion_t(1, 1);
	public static final ProtocolVersion_t PROTOCOLVERSION_2_0 = new ProtocolVersion_t(2, 0);
	public static final ProtocolVersion_t PROTOCOLVERSION_2_1 = new ProtocolVersion_t(2, 1);
	
	private final byte[] bytes;
	
	public ProtocolVersion_t(RTPSByteBuffer bb) {
		this.bytes = new byte[2];
		bb.read(bytes);
	}

	public ProtocolVersion_t(byte[] bytes) {
		this.bytes = bytes; 
		assert bytes.length == 2;
	}
	
	public ProtocolVersion_t(int major, int minor) {
		this(new byte[] {(byte) major, (byte) minor});
	}

	
	public String toString() {
		StringBuffer sb = new StringBuffer("version ");
		sb.append(bytes[0]);
		sb.append('.');
		sb.append(bytes[1]);
		
		return sb.toString();
	}


	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write(bytes);
	}
}
