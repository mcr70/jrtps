package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration;

public class ParticipantLeaseDuration extends Parameter {
    private Duration duration;

    public ParticipantLeaseDuration(Duration duration) {
        super(ParameterEnum.PID_PARTICIPANT_LEASE_DURATION);
        this.duration = duration;
    }

    ParticipantLeaseDuration() {
        super(ParameterEnum.PID_PARTICIPANT_LEASE_DURATION);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.duration = new Duration(bb);
    }

    @Override
    public void writeTo(RTPSByteBuffer buffer) {
        duration.writeTo(buffer);
    }

    public Duration getDuration() {
        return duration;
    }

    public String toString() {
        return super.toString() + ": " + getDuration();
    }
}