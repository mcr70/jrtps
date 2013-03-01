package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class EntityName extends Parameter {
	EntityName() {
		super(ParameterEnum.PID_ENTITY_NAME);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}