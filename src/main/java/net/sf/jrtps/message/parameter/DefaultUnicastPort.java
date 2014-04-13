package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class DefaultUnicastPort extends Parameter {
    DefaultUnicastPort() {
        super(ParameterEnum.PID_DEFAULT_UNICAST_PORT);
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