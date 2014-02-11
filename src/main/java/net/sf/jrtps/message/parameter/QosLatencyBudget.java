package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration;

public class QosLatencyBudget extends Parameter implements DataReaderPolicy<QosLatencyBudget>,
        DataWriterPolicy<QosLatencyBudget>, TopicPolicy<QosLatencyBudget>, InlineParameter {
    private Duration duration;

    QosLatencyBudget() {
        super(ParameterEnum.PID_LATENCY_BUDGET);
    }

    /**
     * Constructor for QosLatenvyBudget.
     * 
     * @param duration
     */
    public QosLatencyBudget(Duration duration) {
        super(ParameterEnum.PID_LATENCY_BUDGET);
        this.duration = duration;
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
    public boolean isCompatible(QosLatencyBudget other) {
        QosLatencyBudget qOther = (QosLatencyBudget) other;
        return duration.asMillis() <= qOther.duration.asMillis();
    }

    /**
     * Get the default QosLatencyBudget: Duration 0,0
     * 
     * @return Default QosLatencyBudget
     */
    public static QosLatencyBudget defaultLatencyBudget() {
        return new QosLatencyBudget(new Duration(0, 0));
    }

    public String toString() {
        return super.toString() + "(" + duration + ")";
    }
}