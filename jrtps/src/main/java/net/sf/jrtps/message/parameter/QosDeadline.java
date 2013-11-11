package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration_t;


/**
 * This policy is useful for cases where a Topic is expected to have each instance updated periodically. 
 * On the publishing side this setting establishes a contract that the application must meet. 
 * On the subscribing side the setting establishes a minimum requirement for the remote publishers 
 * that are expected to supply the data values. <p>
 * 
 * See 7.1.3.7 DEADLINE
 * 
 * @author mcr70
 *
 */
public class QosDeadline extends Parameter implements QosPolicy {
	private Duration_t period;
	QosDeadline() {
		super(ParameterEnum.PID_DEADLINE);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		period = new Duration_t(bb);
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		period.writeTo(bb);
	}

	@Override
	public boolean isCompatible(QosPolicy other) {
		if (other instanceof QosDeadline) {
			QosDeadline qOther = (QosDeadline) other;
			return period.asMillis() <= qOther.period.asMillis();
		}
		return false;
	}
}