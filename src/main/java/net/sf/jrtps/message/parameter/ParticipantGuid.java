package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Guid;

public class ParticipantGuid extends Parameter {
    private Guid guid;

    public ParticipantGuid(Guid guid) {
        this();
        this.guid = guid;
    }

    ParticipantGuid() {
        super(ParameterEnum.PID_PARTICIPANT_GUID);
    }

    public Guid getParticipantGuid() {
        return guid;
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.guid = new Guid(bb);
    }

    @Override
    public void writeTo(RTPSByteBuffer buffer) {
        guid.writeTo(buffer);
    }

    public String toString() {
        return super.toString() + ": " + getParticipantGuid();
    }
}