package alt.rtps.message.parameter;

import alt.rtps.transport.RTPSByteBuffer;
import alt.rtps.types.Duration_t;

public class ParticipantLeaseDuration extends Parameter {
	private Duration_t duration;

	public ParticipantLeaseDuration(Duration_t duration) {
		super(ParameterEnum.PID_PARTICIPANT_LEASE_DURATION);
		this.duration = duration;
	}

	ParticipantLeaseDuration() {
		super(ParameterEnum.PID_PARTICIPANT_LEASE_DURATION);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length)  {
		this.duration = new Duration_t(bb);
	}

	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		duration.writeTo(buffer);
	}
	
	public Duration_t getDuration() {
		return duration;
	}
	
	public String toString() {
		return super.toString() + ": " + getDuration();
	}
}