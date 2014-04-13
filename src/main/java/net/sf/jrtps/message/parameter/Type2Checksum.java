package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

@Deprecated
public class Type2Checksum extends Parameter {
    Type2Checksum() {
        super(ParameterEnum.PID_TYPE2_CHECKSUM);
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