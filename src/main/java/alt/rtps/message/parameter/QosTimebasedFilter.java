package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class QosTimebasedFilter extends Parameter implements QualityOfService {
	QosTimebasedFilter() {
		super(ParameterEnum.PID_TIME_BASED_FILTER);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}