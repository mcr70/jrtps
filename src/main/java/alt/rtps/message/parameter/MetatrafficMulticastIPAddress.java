package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class MetatrafficMulticastIPAddress extends Parameter {
	MetatrafficMulticastIPAddress() {
		super(ParameterEnum.PID_METATRAFFIC_MULTICAST_IPADDRESS);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}