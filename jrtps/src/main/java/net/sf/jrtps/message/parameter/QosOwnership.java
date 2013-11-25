package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;


public class QosOwnership extends Parameter implements DataReaderPolicy<QosOwnership>, TopicPolicy<QosOwnership>, DataWriterPolicy<QosOwnership>, InlineParameter {
	private int kind;

	public enum Kind {
		SHARED, EXCLUSIVE
	}

	QosOwnership() {
		super(ParameterEnum.PID_OWNERSHIP);
	}

	/**
	 * Constructor.
	 * @param kind
	 */
	public QosOwnership(Kind kind) {
		super(ParameterEnum.PID_OWNERSHIP);
		switch(kind) {
		case SHARED: this.kind = 0;		
		case EXCLUSIVE: this.kind = 1;
		}
	}

	@Override
	public void read(RTPSByteBuffer bb, int length) {
		kind = bb.read_long();
	}

	@Override
	public void writeTo(RTPSByteBuffer bb) {
		bb.write_long(kind);
	}
	public Kind getKind() {
		switch(kind) {
		case 0: return Kind.SHARED;
		case 1: return Kind.EXCLUSIVE;
		}

		throw new IllegalArgumentException("Unknown kind " + kind + " for QosOwnership");
	}

	@Override
	public boolean isCompatible(QosOwnership other) {
		return kind == other.kind;
	}

	/**
	 * Get the default QosOwnership: SHARED
	 * @return default QosOwnership
	 */
	public static QosOwnership defaultOwnership() {
		return new QosOwnership(Kind.SHARED); // TODO: check default
	}

	public String toString() {	
		return super.toString() + "(" + getKind() + ")";
	}
}