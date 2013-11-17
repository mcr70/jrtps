package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration_t;


public class QosLatencyBudget extends Parameter implements DataReaderPolicy, DataWriterPolicy, TopicPolicy, InlineParameter {
	private Duration_t duration;
	
	QosLatencyBudget() {
		super(ParameterEnum.PID_LATENCY_BUDGET);
	}

	/**
	 * Constructor for QosLatenvyBudget.
	 * 
	 * @param duration
	 */
	public QosLatencyBudget(Duration_t duration) {
		super(ParameterEnum.PID_LATENCY_BUDGET);
		this.duration = duration;
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

	/**
	 * Get the default QosLatencyBudget: Duration 0,0
	 * 
	 * @return Default QosLatencyBudget
	 */
	public static QosLatencyBudget defaultLatencyBudget() {
		return new QosLatencyBudget(new Duration_t(0, 0));
	}
}