package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration_t;


/**
 * This policy is useful for cases where a Topic is expected to have each instance updated periodically. 
 * On the publishing side this setting establishes a contract that the application must meet. 
 * On the subscribing side the setting establishes a minimum requirement for the remote publishers 
 * that are expected to supply the data values. <p>
 * 
 * This policy must be consistent with QosTimeBasedFilter, so that period <= minimum_separation.
 * 
 * See 7.1.3.7 DEADLINE
 * 
 * @author mcr70
 *
 */
public class QosDeadline extends Parameter implements DataReaderPolicy<QosDeadline>, DataWriterPolicy<QosDeadline>, TopicPolicy<QosDeadline>, InlineParameter {
	private Duration_t period;

	QosDeadline() {
		super(ParameterEnum.PID_DEADLINE);
	}

	public QosDeadline(Duration_t period) {
		super(ParameterEnum.PID_DEADLINE);
		this.period = period;
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		period = new Duration_t(bb);
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		period.writeTo(bb);
	}

	/**
	 * Get the period of this DEADLINE policy.
	 * @return deadline
	 */
	public Duration_t getPeriod() {
		return period;
	}

	/**
	 * Checks, if this QosDeadline is compatible with the other QosPolicy.
	 * 
	 * @return true, if this.period <= other.period
	 */
	@Override
	public boolean isCompatible(QosDeadline other) {
		QosDeadline qOther = (QosDeadline) other;
		return period.asMillis() <= qOther.period.asMillis();
	}

	/**
	 * Gets a default QosDeadline with period Duration_t.INFINITE
	 * @return default QosDeadline
	 */
	public static QosDeadline defaultDeadline() {
		return new QosDeadline(Duration_t.INFINITE);
	}

	public String toString() {
		return super.toString() + "(" + period + ")";
	}	
}