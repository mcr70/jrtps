package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;


public class QosResourceLimits extends Parameter implements DataReaderPolicy, TopicPolicy, DataWriterPolicy {
	private int max_samples;
	private int max_instances;
	private int max_samples_per_instance;

	QosResourceLimits() {
		super(ParameterEnum.PID_RESOURCE_LIMITS);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		max_samples = bb.read_long();
		max_instances = bb.read_long();
		max_samples_per_instance = bb.read_long();
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		bb.write_long(getMaxSamples());
		bb.write_long(getMaxInstances());
		bb.write_long(getMaxSamplesPerInstance());
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
}