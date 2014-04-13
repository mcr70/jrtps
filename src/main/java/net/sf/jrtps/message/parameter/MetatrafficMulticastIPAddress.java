package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class MetatrafficMulticastIPAddress extends Parameter {
    MetatrafficMulticastIPAddress() {
        super(ParameterEnum.PID_METATRAFFIC_MULTICAST_IPADDRESS);
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