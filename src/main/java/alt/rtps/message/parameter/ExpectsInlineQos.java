package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class ExpectsInlineQos extends Parameter {
	ExpectsInlineQos() {
		super(ParameterEnum.PID_EXPECTS_INLINE_QOS);
	}
	
	public boolean expectsInlineQos() {
		return getBytes()[0] == 1; // TODO: Check boolean encoding
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