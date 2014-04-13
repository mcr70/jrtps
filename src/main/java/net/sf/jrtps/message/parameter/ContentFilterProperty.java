package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class ContentFilterProperty extends Parameter {
    ContentFilterProperty() {
        super(ParameterEnum.PID_CONTENT_FILTER_PROPERTY);
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