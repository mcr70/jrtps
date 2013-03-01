package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


@Deprecated
public class ReliabilityEnabled extends Parameter {
	ReliabilityEnabled() {
		super(ParameterEnum.PID_RELIABILITY_ENABLED);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}