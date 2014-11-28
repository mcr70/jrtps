package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * ProtocolVersion parameter.
 * 
 * @author mcr70
 *
 */
public class ProtocolVersion extends Parameter {
    public static final ProtocolVersion PROTOCOLVERSION_1_0 = new ProtocolVersion(1, 0);
    public static final ProtocolVersion PROTOCOLVERSION_1_1 = new ProtocolVersion(1, 1);
    public static final ProtocolVersion PROTOCOLVERSION_2_0 = new ProtocolVersion(2, 0);
    public static final ProtocolVersion PROTOCOLVERSION_2_1 = new ProtocolVersion(2, 1);

    private byte[] bytes;
	
    public ProtocolVersion(byte[] bytes) {
    	super(ParameterId.PID_PROTOCOL_VERSION);
    	this.bytes = bytes;
        
    	if (bytes.length != 2) {
    		throw new IllegalArgumentException("ProtocolVersion length must be 2");
    	}
    }

    public ProtocolVersion(int major, int minor) {
        this(new byte[] { (byte) major, (byte) minor });
    }

    public ProtocolVersion(RTPSByteBuffer bb) {
    	super(ParameterId.PID_PROTOCOL_VERSION);
    	read(bb, 2);
    }
    
    ProtocolVersion() {
        super(ParameterId.PID_PROTOCOL_VERSION);
    }

    /**
     * Gets the major version number
     * @return major
     */
    public byte getMajor() {
        return bytes[0];
    }
    
    /**
     * Gets the minor version number
     * @return minor
     */
    public byte getMinor() {
        return bytes[1];
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.bytes = new byte[2];
        bb.read(bytes);
    }

    @Override
    public void writeTo(RTPSByteBuffer buffer) {
        buffer.write(bytes);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer(super.toString());
        sb.append("(");
        sb.append(bytes[0]);
        sb.append('.');
        sb.append(bytes[1]);
        sb.append(")");
        
        return sb.toString();

    }
}