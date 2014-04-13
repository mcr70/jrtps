package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class TypeMaxSizeSerialized extends Parameter {
    TypeMaxSizeSerialized() {
        super(ParameterEnum.PID_TYPE_MAX_SIZE_SERIALIZED);
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