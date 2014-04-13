package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

@Deprecated
public class ExpectsAck extends Parameter {
    ExpectsAck() {
        super(ParameterEnum.PID_EXPECTS_ACK);
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