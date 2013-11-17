package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;


public class QosOwnership extends Parameter implements DataReaderPolicy, TopicPolicy, DataWriterPolicy, InlineParameter {
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

	@Override
	public boolean isCompatible(QosPolicy other) {
		if (other instanceof QosOwnership) {
			QosOwnership qOther = (QosOwnership) other;
			return kind == qOther.kind;
		}

		return false;
	}

	/**
	 * Get the default QosOwnership: SHARED
	 * @return default QosOwnership
	 */
	public static QosOwnership defaultOwnership() {
		return new QosOwnership(Kind.SHARED); // TODO: check default
	}
}