package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class QosOwnershipStrength extends Parameter implements QualityOfService {
	QosOwnershipStrength() {
		super(ParameterEnum.PID_OWNERSHIP_STRENGTH);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}