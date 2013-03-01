package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class DefaultUnicastIPAddress extends Parameter {
	DefaultUnicastIPAddress() {
		super(ParameterEnum.PID_DEFAULT_UNICAST_IPADDRESS);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}