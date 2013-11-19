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
public class QosLiveliness extends Parameter implements DataReaderPolicy<QosLiveliness>, DataWriterPolicy<QosLiveliness>, TopicPolicy<QosLiveliness>, InlineParameter {
	private int kind;
	private Duration_t lease_duration;

	public enum Kind {
		AUTOMATIC, MANUAL_BY_PARTICIPANT, MANUAL_BY_TOPIC
	}


	QosLiveliness() {
		super(ParameterEnum.PID_LIVELINESS);
	}

	QosLiveliness(Kind kind, Duration_t lease_duration) {
		super(ParameterEnum.PID_LIVELINESS);
		switch(kind) {
		case AUTOMATIC: this.kind = 0; break;
		case MANUAL_BY_PARTICIPANT: this.kind = 1; break;
		case MANUAL_BY_TOPIC: this.kind = 2; break;
		}

		this.lease_duration = lease_duration;
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

		return null;
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
		return super.toString() + "(" + getKind() + ", " + lease_duration + ")";
	}

	@Override
	public boolean isCompatible(QosLiveliness other) {
		if ((kind >= other.kind) && 
				(lease_duration.asMillis() <= other.lease_duration.asMillis())) {
			return true;
		}
		return false;
	}

	public static QosLiveliness defaultLiveliness() {
		return new QosLiveliness(Kind.AUTOMATIC, new Duration_t(10, 0)); // TODO: check defaults
	}
}