package net.sf.jrtps.message.parameter;

import java.util.Arrays;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration_t;


public class QosTimebasedFilter extends Parameter implements QualityOfService {
	private Duration_t minimum_separation;

	public QosTimebasedFilter(Duration_t minimum_separation) {
		super(ParameterEnum.PID_TIME_BASED_FILTER);
		this.minimum_separation = minimum_separation;
		
		// TODO: OSPL 5.5 encodes timebased filter as two bytes: [0,0]
		// when it should encode them as two longs for Duration: [0,0,0,0, 0,0,0,0]
		// TODO: double check this against OpenDDS.
	}
	
	QosTimebasedFilter() {
		super(ParameterEnum.PID_TIME_BASED_FILTER);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
		//minimum_separation = new Duration_t(bb);
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		writeBytes(bb); // TODO: default writing. just writes byte[] in super class
		//minimum_separation.writeTo(bb);
	}

	public String toString() {	
		//return super.toString() + "(" + minimum_separation + ")";
		return super.toString() + "(" + Arrays.toString(getBytes()) + ")";
	}
}