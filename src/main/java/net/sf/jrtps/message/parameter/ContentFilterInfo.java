package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class ContentFilterInfo extends Parameter implements InlineQoS {
    ContentFilterInfo() {
        super(ParameterId.PID_CONTENT_FILTER_INFO);
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