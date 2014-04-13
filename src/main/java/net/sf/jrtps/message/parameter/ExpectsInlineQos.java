package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class ExpectsInlineQos extends Parameter {
    ExpectsInlineQos() {
        super(ParameterEnum.PID_EXPECTS_INLINE_QOS);
    }

    public boolean expectsInlineQos() {
        return getBytes()[0] == 1;
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