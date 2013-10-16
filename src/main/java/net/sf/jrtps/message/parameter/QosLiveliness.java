package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration_t;


public class QosLiveliness extends Parameter implements QualityOfService {
	private int kind;
	private Duration_t lease_duration;

	public enum Kind {
		AUTOMATIC(0), MANUAL_BY_PARTICIPANT(1), MANUAL_BY_TOPIC(2), UNKNOWN_LIVELINESS_KIND(99);

		private int __kind;
		private Kind(int kind) {
			__kind = kind;
		}
	}

	
	QosLiveliness() {
		super(ParameterEnum.PID_LIVELINESS);
	}

	public Duration_t getLeaseDuration() {
		return lease_duration;
	}
	
	public Kind getKind() {
		switch(kind) {
		case 0: return Kind.AUTOMATIC;
		case 1: return Kind.MANUAL_BY_PARTICIPANT;
		case 2: return Kind.MANUAL_BY_TOPIC;
		}
		
		return Kind.UNKNOWN_LIVELINESS_KIND;
	}


	@Override
	public void read(RTPSByteBuffer bb, int length)  {
		this.kind = bb.read_long();
		lease_duration = new Duration_t(bb);
	}
	
	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_long(kind);
		lease_duration.writeTo(buffer);
	}

	public String toString() {	
		return super.toString() + "(" + getKind() + lease_duration + ")";
	}
}