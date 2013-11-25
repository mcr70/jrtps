package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;


public class QosOwnershipStrength extends Parameter implements DataWriterPolicy<QosOwnershipStrength>, InlineParameter {
	private int strength;
	
	QosOwnershipStrength() {
		super(ParameterEnum.PID_OWNERSHIP_STRENGTH);
	}

	/**
	 * Constructor
	 * @param strength
	 */
	public QosOwnershipStrength(int strength) {
		super(ParameterEnum.PID_OWNERSHIP_STRENGTH);
		this.strength = strength;
	}

	/**
	 * Get the strength.
	 * @return strength
	 */
	public int getStrength() {
		return strength;
	}
	
	@Override
	public void read(RTPSByteBuffer bb, int length) {
		strength = bb.read_long();
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		bb.write_long(strength);
	}

	@Override
	public boolean isCompatible(QosOwnershipStrength other) {
		return true; // Always true
	}
	
	/**
	 * Get the default QosOwnershipStrength: 0
	 * @return default QosOwnershipStrength
	 */
	public static QosOwnershipStrength defaultOwnershipStrength() {
		return new QosOwnershipStrength(0);
	}

	public String toString() {
		return super.toString() + "(" + strength + ")";
	}
}