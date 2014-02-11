package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

public class ParticipantManualLivelinessCount extends Parameter {
    private volatile int count;

    /**
     * Constructs a new ParticipantManualLivelinessCount
     */
    public ParticipantManualLivelinessCount() {
        super(ParameterEnum.PID_PARTICIPANT_MANUAL_LIVELINESS_COUNT);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.count = bb.read_long();
    }

    @Override
    public void writeTo(RTPSByteBuffer buffer) {
        buffer.write_long(count);
    }

    public int getCount() {
        return count;
    }
    
    public void incrementCount() {
        count++;
    }
}