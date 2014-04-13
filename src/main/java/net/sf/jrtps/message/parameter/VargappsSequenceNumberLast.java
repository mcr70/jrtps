package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

@Deprecated
public class VargappsSequenceNumberLast extends Parameter {
    VargappsSequenceNumberLast() {
        super(ParameterEnum.PID_VARGAPPS_SEQUENCE_NUMBER_LAST);
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