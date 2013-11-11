package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration_t;


public class QosLatencyBudget extends Parameter implements QosPolicy {
	private Duration_t duration;
	QosLatencyBudget() {
		super(ParameterEnum.PID_LATENCY_BUDGET);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		this.duration = new Duration_t(bb);
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		duration.writeTo(bb);
	}

	@Override
	public boolean isCompatible(QosPolicy other) {
		if (other instanceof QosLatencyBudget) {
			QosLatencyBudget qOther = (QosLatencyBudget) other;
			return duration.asMillis() <= qOther.duration.asMillis();
		}
		
		return false;
	}
}