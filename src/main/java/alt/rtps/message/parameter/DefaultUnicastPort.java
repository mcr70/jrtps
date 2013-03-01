package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class DefaultUnicastPort extends Parameter {
	DefaultUnicastPort() {
		super(ParameterEnum.PID_DEFAULT_UNICAST_PORT);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}