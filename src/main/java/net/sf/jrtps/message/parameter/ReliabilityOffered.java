package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

@Deprecated
public class ReliabilityOffered extends Parameter {
    ReliabilityOffered() {
        super(ParameterEnum.PID_RELIABILITY_OFFERED);
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