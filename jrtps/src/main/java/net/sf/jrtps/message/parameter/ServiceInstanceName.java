package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class ServiceInstanceName extends Parameter {

    protected ServiceInstanceName() {
	super(ParameterId.PID_SERVICE_INSTANCE_NAME);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
	super.readBytes(bb, length);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
	super.writeBytes(bb);
    }
}
