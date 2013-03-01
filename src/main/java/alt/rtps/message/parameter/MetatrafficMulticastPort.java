package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class MetatrafficMulticastPort extends Parameter {
	MetatrafficMulticastPort() {
		super(ParameterEnum.PID_METATRAFFIC_MULTICAST_PORT);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}