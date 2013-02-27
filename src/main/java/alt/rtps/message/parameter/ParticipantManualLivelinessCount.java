package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;


public class ParticipantManualLivelinessCount extends Parameter {
	int count;
	
	ParticipantManualLivelinessCount() {
		super(ParameterEnum.PID_PARTICIPANT_MANUAL_LIVELINESS_COUNT);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length)  {
		this.count = bb.read_long();
	}
	
	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_long(count);
	}
	
	public int getCount() {
		return count;
	}
}