package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;

/**
 * QosResourceLimits.
 * This policy must be consistent with QosHistory, so that HISTORY.depth <= RESOURCE_LIMITS.max_samples_per_instance.
 * Also, max_samples >= max_samples_per_instance
 * 
 * @author mcr70
 */
public class QosResourceLimits extends Parameter implements DataReaderPolicy, TopicPolicy, DataWriterPolicy {
	private int max_samples;
	private int max_instances;
	private int max_samples_per_instance;

	QosResourceLimits() {
		super(ParameterEnum.PID_RESOURCE_LIMITS);
	}

	public QosResourceLimits(int max_samples, int max_instances, int max_samples_per_instance) {
		super(ParameterEnum.PID_RESOURCE_LIMITS);
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
	public boolean isCompatible(QosPolicy other) {
		return true; // Always true. TODO: must be consistent with QosHistory
	}

	/**
	 * Get the default QosResouceLimits: LENGTH_UNLIMITED, LENGTH_UNLIMITED, LENGTH_UNLIMITED
	 * @return default QosResouceLimits
	 */
	public static QosResourceLimits defaultResourceLimits() {
		return new QosResourceLimits(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}
}