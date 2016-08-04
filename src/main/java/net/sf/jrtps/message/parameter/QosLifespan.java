package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration;

/**
 * QosLifespan policy is used to automatically remove samples from history cache
 * of entity.
 * 
 * See DDS specification v1.2, ch 7.1.3.16
 * 
 * @author mcr70
 */
public class QosLifespan extends Parameter implements DataReaderPolicy<QosLifespan>, DataWriterPolicy<QosLifespan>,
        TopicPolicy<QosLifespan>, InlineQoS, Changeable {
    private Duration duration;

    QosLifespan() {
        super(ParameterId.PID_LIFESPAN);
    }

    /**
     * Constructor for QosLifespan.
     * @param millis Duration as millis
     */
    public QosLifespan(long millis) {
        this(new Duration(millis));
    }
    
    /**
     * Constructor for QosLifespan.
     * @param duration Duration
     */
    public QosLifespan(Duration duration) {
        super(ParameterId.PID_LIFESPAN);
        this.duration = duration;
    }

    /**
     * Get the Duration of lifespan
     * @return Duration
     */
    public Duration getDuration() {
        return duration;
    }
    
    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.duration = new Duration(bb);
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        duration.writeTo(bb);
    }

    @Override
    public boolean isCompatible(QosLifespan other) {
        return true; // Always true.
    }

    /**
     * Get the default QosLifespan with infinite duration.
     * 
     * @return default QosLifespan
     */
    public static QosLifespan defaultLifespan() {
        return new QosLifespan(Duration.INFINITE);
    }

    public String toString() {
        return super.toString() + "(" + duration + ")";
    }
}