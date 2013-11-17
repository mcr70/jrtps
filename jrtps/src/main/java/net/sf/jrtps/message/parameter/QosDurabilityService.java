package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;


public class QosDurabilityService extends Parameter implements DataWriterPolicy, TopicPolicy {
	QosDurabilityService() {
		super(ParameterEnum.PID_DURABILITY_SERVICE);
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
		return true; // Always true // TODO: check this
	}

	public static QosPolicy defaultDurabilityService() {
		// TODO Auto-generated method stub
		return null;
	}
}