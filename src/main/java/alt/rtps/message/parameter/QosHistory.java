package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class QosHistory extends Parameter implements QualityOfService {
	QosHistory() {
		super(ParameterEnum.PID_HISTORY);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}