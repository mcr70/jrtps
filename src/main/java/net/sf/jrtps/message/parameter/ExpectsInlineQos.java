package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class ExpectsInlineQos extends Parameter {
    private boolean expectsInlineQos;
    
    ExpectsInlineQos() {
        super(ParameterEnum.PID_EXPECTS_INLINE_QOS);
    }

    public ExpectsInlineQos(boolean b) {
        super(ParameterEnum.PID_EXPECTS_INLINE_QOS);
        expectsInlineQos = b;
    }
    
    public boolean expectsInlineQos() {
        return getBytes()[0] == 1;
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        byte b = bb.read_octet();
        if (b == 0) {
            expectsInlineQos = false;
        }
        else {
            expectsInlineQos = true;
        }
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        if (expectsInlineQos) {
            bb.write_octet((byte) 1);
        }
        else {
            bb.write_octet((byte) 0);
        }
    }
}