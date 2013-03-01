package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class QosLifespan extends Parameter implements QualityOfService {
	QosLifespan() {
		super(ParameterEnum.PID_LIFESPAN);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}