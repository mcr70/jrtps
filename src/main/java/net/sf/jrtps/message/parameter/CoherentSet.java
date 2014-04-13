package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class CoherentSet extends Parameter implements InlineParameter {
    CoherentSet() {
        super(ParameterEnum.PID_COHERENT_SET);
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