package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * QosResourceLimits. This policy must be consistent with QosHistory, so that
 * HISTORY.depth &le; RESOURCE_LIMITS.max_samples_per_instance. Also, max_samples
 * &ge; max_samples_per_instance
 * 
 * @author mcr70
 */
public class QosResourceLimits extends Parameter implements DataReaderPolicy<QosResourceLimits>,
        TopicPolicy<QosResourceLimits>, DataWriterPolicy<QosResourceLimits> {
    private int max_samples;
    private int max_instances;
    private int max_samples_per_instance;

    QosResourceLimits() {
        super(ParameterId.PID_RESOURCE_LIMITS);
    }

    /**
     * Constructs QosResourceLimits. A value of -1 indicates a given resource constraint is 
     * disabled.
     * @param max_samples Maximum number of total samples allowed
     * @param max_instances Maximum number of instances allowed
     * @param max_samples_per_instance Maximum number of samples per instance allowed
     */
    public QosResourceLimits(int max_samples, int max_instances, int max_samples_per_instance) {
        super(ParameterId.PID_RESOURCE_LIMITS);
        this.max_samples = max_samples;
        this.max_instances = max_instances;
        this.max_samples_per_instance = max_samples_per_instance;
    }

    @Override
    public void read(RTPSByteBuffer bb, int length) {
        max_samples = bb.read_long();
        max_instances = bb.read_long();
        max_samples_per_instance = bb.read_long();
    }

    @Override
    public void writeTo(RTPSByteBuffer bb) {
        bb.write_long(max_samples);
        bb.write_long(max_instances);
        bb.write_long(max_samples_per_instance);
    }

    /**
     * Get max_samples
     * @return max_samples
     */
    public int getMaxSamples() {
        return max_samples;
    }

    /**
     * Get max_instances
     * @return max_instances
     */
    public int getMaxInstances() {
        return max_instances;
    }

    /**
     * Get max_samples_per_instance
     * @return max_samples_per_instance
     */
    public int getMaxSamplesPerInstance() {
        return max_samples_per_instance;
    }

    @Override
    public boolean isCompatible(QosResourceLimits other) {
        return true; // Always true
    }

    /**
     * Get the default QosResouceLimits: LENGTH_UNLIMITED, LENGTH_UNLIMITED,
     * LENGTH_UNLIMITED
     * 
     * @return default QosResouceLimits
     */
    public static QosResourceLimits defaultResourceLimits() {
        return new QosResourceLimits(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public String toString() {
        return super.toString() + "(max_instances " + max_instances + ", max_samples " + max_samples
                + ", max_samples_per_instance " + max_samples_per_instance + ")";
    }
}