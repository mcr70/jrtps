package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class QosTransportPriority extends Parameter implements QualityOfService {
	QosTransportPriority() {
		super(ParameterEnum.PID_TRANSPORT_PRIORITY);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}