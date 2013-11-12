package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;


public class QosTopicData extends Parameter implements DataReaderPolicy, DataWriterPolicy, TopicPolicy {
	QosTopicData() {
		super(ParameterEnum.PID_TOPIC_DATA);
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		readBytes(bb, length); // TODO: default reading. just reads to byte[] in super class.
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		writeBytes(bb); // TODO: default writing. just writes byte[] in super class
	}

	@Override
	public boolean isCompatible(QosPolicy other) {
		return true; // Always true
	}
}