package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class QosDeadline extends Parameter implements QualityOfService {
	QosDeadline() {
		super(ParameterEnum.PID_DEADLINE);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		writeBytes(bb); // TODO: default writing. just writes byte[] in super class
	}
}