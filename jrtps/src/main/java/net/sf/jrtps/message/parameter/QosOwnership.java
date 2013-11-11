package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;


public class QosOwnership extends Parameter implements QosPolicy {
	private int kind;
	
	public enum Kind {
		SHARED, EXCLUSIVE
	}
	
	QosOwnership() {
		super(ParameterEnum.PID_OWNERSHIP);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		kind = bb.read_long();
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		bb.write_long(kind);
	}

	@Override
	public boolean isCompatible(QosPolicy other) {
		if (other instanceof QosOwnership) {
			QosOwnership qOther = (QosOwnership) other;
			return kind == qOther.kind;
		}
		
		return false;
	}
}