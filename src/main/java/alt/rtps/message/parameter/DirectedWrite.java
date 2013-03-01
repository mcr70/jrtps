package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class DirectedWrite extends Parameter {
	DirectedWrite() {
		super(ParameterEnum.PID_DIRECTED_WRITE);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}