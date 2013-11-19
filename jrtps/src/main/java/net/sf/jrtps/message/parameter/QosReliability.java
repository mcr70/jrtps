package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration_t;


public class QosReliability extends Parameter implements DataReaderPolicy<QosReliability>, DataWriterPolicy<QosReliability>, TopicPolicy<QosReliability>, InlineParameter {
	private int kind;
	private Duration_t max_blocking_time;
	
	public enum Kind {
		BEST_EFFORT, RELIABLE;
	}

	QosReliability() {
		super(ParameterEnum.PID_RELIABILITY);
	}

	public QosReliability(Kind kind, Duration_t max_blocking_time) {
		super(ParameterEnum.PID_RELIABILITY);
		switch(kind) {
		case BEST_EFFORT: this.kind = 0; break;
		case RELIABLE: this.kind = 1;
		}
		this.max_blocking_time = max_blocking_time;
		//this.kind = kind.ordinal() + 1; // TODO: OSPL 5.5 uses KIND=2, maybe there is an offset error like 1 for BEST_EFFORT and 2 for RELIABLE
	}

	public Duration_t getMaxBlockingTime() {
		return max_blocking_time;
	}
	
	public Kind getKind() {
		switch(kind) {
		case 0: return Kind.BEST_EFFORT;
		case 1: return Kind.RELIABLE; 
		}

		return null;
	}


	@Override
	public void read(RTPSByteBuffer bb, int length)  {
		this.kind = bb.read_long(); // TODO: OSPL 5.5 uses KIND=2, maybe there is an offset error like 1 for BEST_EFFORT and 2 for RELIABLE 
		max_blocking_time = new Duration_t(bb);
	}
	
	@Override
	public void writeTo(RTPSByteBuffer buffer) {
		buffer.write_long(kind);
		max_blocking_time.writeTo(buffer);
	}

	public String toString() {	
		return super.toString() + "(" + getKind() + max_blocking_time + ")";
	}

	@Override
	public boolean isCompatible(QosReliability other) {
		return kind >= other.kind;
	}

	/**
	 * get the default QosReliability: BEST_EFFORT, 0
	 * 
	 * @return default QosReliability
	 */
	public static QosReliability defaultReliability() {
		return new QosReliability(Kind.BEST_EFFORT, new Duration_t(0, 0)); // TODO: check default
	}
}