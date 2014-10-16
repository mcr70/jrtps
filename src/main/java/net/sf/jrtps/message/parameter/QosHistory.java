package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * QosHistory. This policy must be consistent with QosResourceLimits, so that
 * HISTORY.depth <= RESOURCE_LIMITS.max_samples_per_instance
 * 
 * @author mcr70
 * 
 */
public class QosHistory extends Parameter implements DataReaderPolicy<QosHistory>, TopicPolicy<QosHistory>,
        DataWriterPolicy<QosHistory> {
    private int kind;
    private int depth;

    public enum Kind {
        KEEP_LAST, KEEP_ALL;
    }

    QosHistory() {
        super(ParameterId.PID_HISTORY);
    }

    /**
     * Constructs QosHistory with kind KEEP_LAST and given depth
     * @param depth
     */
    public QosHistory(int depth) {
        this(Kind.KEEP_LAST, depth);
    }
    
    /**
     * Constructs QosHistory given kind and depth
     * @param depth
     */
    public QosHistory(Kind kind, int depth) {
        super(ParameterId.PID_HISTORY);

        switch (kind) {
        case KEEP_LAST:
            this.kind = 0;
            break;
        case KEEP_ALL:
            this.kind = 1;
            break;
        }

        this.depth = depth;
    }

    /**
     * Get the depth of this QosHistory. Depth 1 means that only latest sample
     * is kept.
     * 
     * @return depth
     */
    public int getDepth() {
        return depth;
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.kind = bb.read_long();
        this.depth = bb.read_long();
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(kind);
        bb.write_long(depth);
    }

    public Kind getKind() {
        switch (kind) {
        case 0:
            return Kind.KEEP_LAST;
        case 1:
            return Kind.KEEP_ALL;
        }

        throw new IllegalArgumentException("Unknown kind " + kind + " for QosHistory");
    }

    @Override
    public boolean isCompatible(QosHistory other) {
        return true; // Always true. 
    }

    /**
     * Get the default QosHistory: KEEP_LAST, 1
     * 
     * @return default QosHistory
     */
    public static QosHistory defaultHistory() {
        return new QosHistory(Kind.KEEP_LAST, 1);
    }

    public String toString() {
        return super.toString() + "(" + getKind() + ", " + depth + ")";
    }
}