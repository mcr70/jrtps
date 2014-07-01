package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class OriginalWriterInfo extends Parameter implements InlineQoS {
    OriginalWriterInfo() {
        super(ParameterEnum.PID_ORIGINAL_WRITER_INFO);
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