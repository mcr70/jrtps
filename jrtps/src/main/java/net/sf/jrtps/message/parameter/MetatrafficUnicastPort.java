package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class MetatrafficUnicastPort extends Parameter {
    MetatrafficUnicastPort() {
        super(ParameterId.PID_METATRAFFIC_UNICAST_PORT);
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