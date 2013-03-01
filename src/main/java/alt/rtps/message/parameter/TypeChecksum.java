package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


@Deprecated
public class TypeChecksum extends Parameter {
	TypeChecksum() {
		super(ParameterEnum.PID_TYPE_CHECKSUM);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}