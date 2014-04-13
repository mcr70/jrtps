package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

@Deprecated
public class ReliabilityEnabled extends Parameter {
    ReliabilityEnabled() {
        super(ParameterEnum.PID_RELIABILITY_ENABLED);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        readBytes(bb, length);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        writeBytes(bb);
    }
}