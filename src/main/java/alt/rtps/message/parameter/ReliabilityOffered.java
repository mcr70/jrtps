package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


@Deprecated
public class ReliabilityOffered extends Parameter {
	ReliabilityOffered() {
		super(ParameterEnum.PID_RELIABILITY_OFFERED);
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