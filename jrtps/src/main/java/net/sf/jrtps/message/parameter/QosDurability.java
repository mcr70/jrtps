package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * This QoS policy controls whether the Service will actually make data available to late-joining readers.<p>
 * 
 * See DDS specification v1.2, ch. 7.1.3.4
 * 
 * @author mcr70
 *
 */
public class QosDurability extends Parameter implements DataReaderPolicy, DataWriterPolicy, TopicPolicy, InlineParameter {
	private int kind;

	public enum Kind {
		VOLATILE(0), TRANSIENT_LOCAL(1), TRANSIENT(2), PERSISTENT(3), UNKNOWN_DURABILITY_KIND(99);

		private int __kind;
		private Kind(int kind) {
			__kind = kind;
		}
	}
	
	QosDurability() {
		super(ParameterEnum.PID_DURABILITY);
	}
	
	@Override
	public void read(RTPSByteBuffer bb, int length)  {
		this.kind = bb.read_long();
	}
	
	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_long(kind);
	}
	
	public Kind getKind() {
		switch(kind) {
		case 0: return Kind.VOLATILE;
		case 1: return Kind.TRANSIENT_LOCAL;
		case 2: return Kind.TRANSIENT;
		case 3: return Kind.PERSISTENT;
		}
		
		return Kind.UNKNOWN_DURABILITY_KIND;
	}
	
	public String toString() {
		return super.toString() + "(" + getKind() + ")";
	}

	@Override
	public boolean isCompatible(QosPolicy other) {
		if (other instanceof QosDurability) {
			QosDurability qOther = (QosDurability) other;
			return kind >= qOther.kind;
		}
		
		return false;
	}
}