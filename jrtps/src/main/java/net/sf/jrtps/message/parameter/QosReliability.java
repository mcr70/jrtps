package net.sf.jrtps.message.parameter;

import net.sf.jrtps.transport.RTPSByteBuffer;
import net.sf.jrtps.types.Duration_t;


public class QosReliability extends Parameter implements DataReaderPolicy, DataWriterPolicy, TopicPolicy, InlineParameter {
	private int kind;
	private Duration_t max_blocking_time;
	
	public enum Kind {
		BEST_EFFORT, RELIABLE, UNKNOWN_RELIABILITY_KIND;
	}

	public QosReliability(Kind kind, Duration_t max_blocking_time) {
		super(ParameterEnum.PID_RELIABILITY);
		this.max_blocking_time = max_blocking_time;
		this.kind = kind.ordinal() + 1; // TODO: OSPL 5.5 uses KIND=2, maybe there is an offset error like 1 for BEST_EFFORT and 2 for RELIABLE
	}
	
	QosReliability() {
		super(ParameterEnum.PID_RELIABILITY);
	}


	public Duration_t getMaxBlockingTime() {
		return max_blocking_time;
	}
	
	public Kind getKind() {
		switch(kind) {
		case 0: return Kind.BEST_EFFORT;
		case 1: return Kind.RELIABLE; 
		}

		return Kind.UNKNOWN_RELIABILITY_KIND;
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
	public boolean isCompatible(QosPolicy other) {
		if (other instanceof QosReliability) {
			QosReliability qOther = (QosReliability) other;
			
			return kind >= qOther.kind;
		}
		return false;
	}
}