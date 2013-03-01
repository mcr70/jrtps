package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class QosDestinationOrder extends Parameter implements QualityOfService {
	QosDestinationOrder() {
		super(ParameterEnum.PID_DESTINATION_ORDER);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}