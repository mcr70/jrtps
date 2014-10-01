package net.sf.jrtps.message.parameter;

import net.sf.jrtps.message.parameter.QosHistory.Kind;
import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration;

public class QosDurabilityService extends Parameter implements DataWriterPolicy<QosDurabilityService>,
        TopicPolicy<QosDurabilityService> {
    private Duration service_cleanup_delay;
    private int history_kind;
    private int history_depth;
    private int max_samples;
    private int max_instances;
    private int max_samples_per_instance;
    
    QosDurabilityService() {
        super(ParameterEnum.PID_DURABILITY_SERVICE);
        
        service_cleanup_delay = new Duration(0);
        history_kind = 0; // KEEP_LAST
        history_depth = 1;
        max_samples = Integer.MAX_VALUE;
        max_instances = Integer.MAX_VALUE;
        max_samples_per_instance = Integer.MAX_VALUE;
    }

    public Duration getService_cleanup_delay() {
        return service_cleanup_delay;
    }
    
    public Kind getHistoryKind() {
        switch (history_kind) {
        case 0 : return QosHistory.Kind.KEEP_LAST;
        case 1 : return QosHistory.Kind.KEEP_ALL;
        
        default:
            throw new RuntimeException("Unknown kind " + history_kind);
        }
    }
    
    public int getHistoryDepth() {
        return history_depth;
    }
    
    public int getMaxSamples() {
        return max_samples;
    }
    
    public int getMaxInstances() {
        return max_instances;
    }
    
    public int getMaxSamplesPerInstance() {
        return max_samples_per_instance;
    }
    
    @Override
    public void read(RTPSByteBuffer bb, int length) {
        this.service_cleanup_delay = new Duration(bb);
        QosHistory history = new QosHistory();
        history.read(bb, 8);
        this.history_kind = bb.read_long();
        this.history_depth = bb.read_long();
        this.max_samples = bb.read_long();
        this.max_instances = bb.read_long();
        this.max_samples_per_instance = bb.read_long();
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        service_cleanup_delay.writeTo(bb);
        bb.write_long(history_kind);
        bb.write_long(history_depth);
        bb.write_long(max_samples);
        bb.write_long(max_instances);
        bb.write_long(max_samples_per_instance);
    }

    @Override
    public boolean isCompatible(QosDurabilityService other) {
        return true; // Always true
    }

    public static QosDurabilityService defaultDurabilityService() {
        return new QosDurabilityService();
    }
}