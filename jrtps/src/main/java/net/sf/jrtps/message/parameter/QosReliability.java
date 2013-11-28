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
		case BEST_EFFORT: this.kind = 1; break;
		case RELIABLE: this.kind = 3; break; // 3: see Table 9.4 - PSM mapping of the value types that appear on the wire
		}
		
		this.max_blocking_time = max_blocking_time;
	}

	public Duration_t getMaxBlockingTime() {
		return max_blocking_time;
	}
	
	public Kind getKind() {
		switch(kind) {
		case 1: return Kind.BEST_EFFORT;
		case 3: return Kind.RELIABLE; // 3: see Table 9.4 - PSM mapping of the value types that appear on the wire 
		}

		throw new IllegalArgumentException("Illegal kind " + kind + " for QosReliability");
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

	public String toString() {	
		return super.toString() + "(" + getKind() + ", "+ max_blocking_time + ")";
	}
}