package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class MetatrafficUnicastIPAddress extends Parameter {
	MetatrafficUnicastIPAddress() {
		super(ParameterEnum.PID_METATRAFFIC_UNICAST_IPADDRESS);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}