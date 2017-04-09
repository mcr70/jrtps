package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class MulticastIPAddress extends Parameter {
    MulticastIPAddress() {
        super(ParameterId.PID_MULTICAST_IPADDRESS);
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