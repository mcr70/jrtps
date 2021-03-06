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
        this(new Duration(0), QosHistory.Kind.KEEP_LAST, 1, 
                Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public QosDurabilityService(Duration serviceCleanupDelay,
            QosHistory.Kind historyKind, int historyDepth, int maxSamples, int maxInstances,
            int maxSamplesPerInstance) {
        super(ParameterId.PID_DURABILITY_SERVICE);
        
        this.service_cleanup_delay = serviceCleanupDelay;
        this.history_kind = historyKind == Kind.KEEP_LAST ? 0 : 1;
        this.history_depth = historyDepth;
        this.max_samples = maxSamples;
        this.max_instances = maxInstances;
        this.max_samples_per_instance = maxSamplesPerInstance;
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