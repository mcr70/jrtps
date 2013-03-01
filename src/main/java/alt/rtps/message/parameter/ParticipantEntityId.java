package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class ParticipantEntityId extends Parameter {
	ParticipantEntityId() {
		super(ParameterEnum.PID_PARTICIPANT_ENTITYID);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}
}