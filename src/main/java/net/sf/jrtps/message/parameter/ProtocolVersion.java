package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.ProtocolVersion_t;

/**
 * ProtocolVersion parameter.
 * 
 * @author mcr70
 *
 */
public class ProtocolVersion extends Parameter {
    private ProtocolVersion_t version;

    public ProtocolVersion(ProtocolVersion_t version) {
        this();
        this.version = version;
    }

    ProtocolVersion() {
        super(ParameterEnum.PID_PROTOCOL_VERSION);
    }

    /**
     * Gets the ProtocolVersion_t.
     * @return ProtocolVersion_t
     */
    public ProtocolVersion_t getProtocolVersion() {
        return version;
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.version = new ProtocolVersion_t(bb);
    }

    @Override
    public void writeTo(RTPSByteBuffer buffer) {
        version.writeTo(buffer);
    }

    public String toString() {
        return super.toString() + ": " + getProtocolVersion();
    }
}