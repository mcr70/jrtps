package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration_t;

/**
 * This policy controls the mechanism and parameters used by the Service to ensure that particular entities on the network
 * are still “alive.”<p>
 * 
 * See DDS specification v1.2, 7.1.3.11 Liveliness
 * 
 * @author mcr70
 *
 */
public class QosLiveliness extends Parameter implements QosPolicy {
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

	@Override
	public boolean isCompatible(QosPolicy other) {
		if (other instanceof QosLiveliness) {
			QosLiveliness qOther = (QosLiveliness) other;
			if (kind >= qOther.kind && lease_duration.asMillis() <= qOther.lease_duration.asMillis()) {
				return true;
			}
		}
		return false;
	}
}