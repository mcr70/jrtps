package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class MetatrafficUnicastIPAddress extends Parameter {
    MetatrafficUnicastIPAddress() {
        super(ParameterEnum.PID_METATRAFFIC_UNICAST_IPADDRESS);
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