package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class QosLatencyBudget extends Parameter implements QualityOfService {
	QosLatencyBudget() {
		super(ParameterEnum.PID_LATENCY_BUDGET);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}