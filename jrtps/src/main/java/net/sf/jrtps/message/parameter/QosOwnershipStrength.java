package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;


public class QosOwnershipStrength extends Parameter implements QosPolicy {
	private int strength;
	
	QosOwnershipStrength() {
		super(ParameterEnum.PID_OWNERSHIP_STRENGTH);
	}

	public int getStrength() {
		return strength;
	}
	
	@Override
	public void read(RTPSByteBuffer bb, int length) {
		strength = bb.read_long();
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		bb.write_long(strength);
	}

	@Override
	public boolean isCompatible(QosPolicy other) {
		return true; // Always true
	}
}