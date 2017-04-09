package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class Sentinel extends Parameter implements InlineQoS {
    public Sentinel() {
        super(ParameterId.PID_SENTINEL);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        readBytes(bb, length);
    	bb.align(4);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
    	bb.align(4);
    	// buffer.write_short(getParameterId().kind());
        // System.out.println("*** " + buffer.position() + ", " +
        // buffer.position() %4);
        // buffer.write_short(2);
        // NO LENGTH
    }
}