package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class QoSDurabilityService extends Parameter implements QualityOfService {
	QoSDurabilityService() {
		super(ParameterEnum.PID_DURABILITY_SERVICE);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}