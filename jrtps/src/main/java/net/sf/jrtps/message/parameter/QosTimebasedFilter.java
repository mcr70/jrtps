package net.sf.jrtps.message.parameter;

import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration_t;


public class QosTimebasedFilter extends Parameter implements DataReaderPolicy {
	private Duration_t minimum_separation;

	public QosTimebasedFilter(Duration_t minimum_separation) {
		super(ParameterEnum.PID_TIME_BASED_FILTER);
		this.minimum_separation = minimum_separation;
		
		// TODO: OSPL 5.5 encodes timebased filter as two bytes: [0,0]
	}
	
	QosTimebasedFilter() {
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

	public String toString() {	
		return super.toString() + "(" + Arrays.toString(getBytes()) + ")";
	}

	@Override
	public boolean isCompatible(QosPolicy other) {
		return true; // Always true. TODO: must be consistent with QosDeadline
	}
}