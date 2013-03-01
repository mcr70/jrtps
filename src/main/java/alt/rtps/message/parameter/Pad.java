package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class Pad extends Parameter {
	Pad() {
		super(ParameterEnum.PID_PAD);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}