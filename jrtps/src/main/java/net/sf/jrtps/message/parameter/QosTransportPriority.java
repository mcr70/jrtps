package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;


public class QosTransportPriority extends Parameter implements QosPolicy, InlineParameter {
	private int value;

	QosTransportPriority() {
		super(ParameterEnum.PID_TRANSPORT_PRIORITY);
	}

	public int getValue() {
		return value;
	}
	
	@Override
	public void read(RTPSByteBuffer bb, int length) {
		value = bb.read_long();
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		bb.write_long(value);
	}

	@Override
	public boolean isCompatible(QosPolicy other) {
		return true; // Always true
	}
}