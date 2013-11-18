package net.sf.jrtps.message.parameter;

import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration_t;

/**
 * QosTimeBasedFilter.
 * This policy must be consistent with QosDeadline, so that period <= minimum_separation.
 * 
 * @author mcr70
 *
 */
public class QosTimeBasedFilter extends Parameter implements DataReaderPolicy {
	private Duration_t minimum_separation;

	public QosTimeBasedFilter(Duration_t minimum_separation) {
		super(ParameterEnum.PID_TIME_BASED_FILTER);
		this.minimum_separation = minimum_separation;
		// TODO: OSPL 5.5 encodes timebased filter as two bytes: [0,0]
	}
	
	QosTimeBasedFilter() {
		super(ParameterEnum.PID_TIME_BASED_FILTER);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		minimum_separation = new Duration_t(bb);
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		minimum_separation.writeTo(bb);
	}

	/**
	 * Get the minimum separation.
	 * @return
	 */
	public Duration_t getMinimumSeparation() {
		return minimum_separation;
	}
	
	public String toString() {	
		return super.toString() + "(" + Arrays.toString(getBytes()) + ")";
	}

	@Override
	public boolean isCompatible(QosPolicy other) {
		return true; // Always true. TODO: must be consistent with QosDeadline
	}

	/**
	 * Get the default QosTimeBasedFilter: 0,0
	 * 
	 * @return default QosTimeBasedFilter
	 */
	public static QosTimeBasedFilter defaultTimeBasedFilter() {
		return new QosTimeBasedFilter(new Duration_t(0, 0));
	}
}