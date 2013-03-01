package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class TypeMaxSizeSerialized extends Parameter {
	TypeMaxSizeSerialized() {
		super(ParameterEnum.PID_TYPE_MAX_SIZE_SERIALIZED);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}