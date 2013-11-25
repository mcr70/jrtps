package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;


public class QosTransportPriority extends Parameter implements TopicPolicy<QosTransportPriority>, DataWriterPolicy<QosTransportPriority>, InlineParameter {
	private int value;

	QosTransportPriority() {
		super(ParameterEnum.PID_TRANSPORT_PRIORITY);
	}

	QosTransportPriority(int value) {
		super(ParameterEnum.PID_TRANSPORT_PRIORITY);
		this.value = value;
	}

	public int getValue() {
		return value;
	}
	
	@Override
	public void read(RTPSByteBuffer bb, int length) {
		value = bb.read_long();
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		bb.write_long(value);
	}

	@Override
	public boolean isCompatible(QosTransportPriority other) {
		return true; // Always true
	}

	/**
	 * Get the default QosTransportPriority: 0
	 * @return default QosTransportPriority
	 */
	public static QosTransportPriority defaultTransportPriority() {
		return new QosTransportPriority(0);
	}

	public String toString() {
		return super.toString() + "(" + value + ")";
	}
}