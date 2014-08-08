package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration;

public class QosReliability extends Parameter implements DataReaderPolicy<QosReliability>,
        DataWriterPolicy<QosReliability>, TopicPolicy<QosReliability>, InlineQoS {
    // see Table 9.4 - PSM mapping of the value types that appear on the wire
    // TODO: OSPL 5.5 uses KIND=2, maybe there is an offset error like 1 for
    // BEST_EFFORT and 2 for RELIABLE
    private static final int BEST_EFFORT_INT = 1;
    private static final int RELIABLE_INT = 2;

    private int kind;
    private Duration max_blocking_time;

    public enum Kind {
        BEST_EFFORT, RELIABLE;
    }

    QosReliability() {
        super(ParameterEnum.PID_RELIABILITY);
    }

    public QosReliability(Kind kind, Duration max_blocking_time) {
        super(ParameterEnum.PID_RELIABILITY);
        switch (kind) {
        case BEST_EFFORT:
            this.kind = BEST_EFFORT_INT;
            break;
        case RELIABLE:
            this.kind = RELIABLE_INT;
            break;
        }

        this.max_blocking_time = max_blocking_time;
    }

    public Duration getMaxBlockingTime() {
        return max_blocking_time;
    }

    public Kind getKind() {
        switch (kind) {
        case BEST_EFFORT_INT:
            return Kind.BEST_EFFORT;
        //case 2: // TODO: OSPL treats 2 as reliable
        case RELIABLE_INT:
            return Kind.RELIABLE;
        }

        throw new IllegalArgumentException("Illegal kind " + kind + " for QosReliability");
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.kind = bb.read_long();
        max_blocking_time = new Duration(bb);
    }

    @Override
    public void writeTo(RTPSByteBuffer buffer) {
        buffer.write_long(kind);
        max_blocking_time.writeTo(buffer);
    }

    @Override
    public boolean isCompatible(QosReliability other) {
        if (other != null) {
            return kind >= other.kind;
        }
        
        return false;
    }

    /**
     * get the default QosReliability: BEST_EFFORT, 0
     * 
     * @return default QosReliability
     */
    public static QosReliability defaultReliability() {
        return new QosReliability(Kind.BEST_EFFORT, new Duration(0, 0));
    }

    public String toString() {
        return super.toString() + "(" + getKind() + ", " + max_blocking_time + ")";
    }
}