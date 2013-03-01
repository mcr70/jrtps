package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class GroupGuid extends Parameter {
	GroupGuid() {
		super(ParameterEnum.PID_GROUP_GUID);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}