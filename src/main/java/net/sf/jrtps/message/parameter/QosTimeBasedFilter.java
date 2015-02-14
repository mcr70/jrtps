package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration;

/**
 * QosTimeBasedFilter. This policy must be consistent with QosDeadline, so that
 * period <= minimum_separation.
 * 
 * @author mcr70
 * 
 */
public class QosTimeBasedFilter extends Parameter implements DataReaderPolicy<QosTimeBasedFilter>, Changeable{
    private Duration minimum_separation;

    public QosTimeBasedFilter(Duration minimum_separation) {
        super(ParameterId.PID_TIME_BASED_FILTER);
        this.minimum_separation = minimum_separation;
        // TODO: OSPL 5.5 encodes timebased filter as two bytes: [0,0]
    }

    /**
     * Constructs QosTimeBasedFilter.
     * @param millis minimum_separation as milliseconds
     */
    public QosTimeBasedFilter(long millis) {
        this(new Duration(millis));
    }
    
    QosTimeBasedFilter() {
        super(ParameterId.PID_TIME_BASED_FILTER);
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        minimum_separation = new Duration(bb);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        minimum_separation.writeTo(bb);
    }

    /**
     * Get the minimum separation.
     * 
     * @return minimum separation
     */
    public Duration getMinimumSeparation() {
        return minimum_separation;
    }

    @Override
    public boolean isCompatible(QosTimeBasedFilter other) {
        return true; // Always true. TODO: must be consistent with QosDeadline
    }

    /**
     * Get the default QosTimeBasedFilter: 0,0
     * 
     * @return default QosTimeBasedFilter
     */
    public static QosTimeBasedFilter defaultTimeBasedFilter() {
        return new QosTimeBasedFilter(new Duration(0, 0));
    }

    public String toString() {
        return super.toString() + "(" + minimum_separation + ")";
    }
}